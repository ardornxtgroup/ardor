/*
 * Copyright © 2013-2016 The Nxt Core Developers.
 * Copyright © 2016-2018 Jelurida IP B.V.
 *
 * See the LICENSE.txt file at the top-level directory of this distribution
 * for licensing information.
 *
 * Unless otherwise agreed in a custom licensing agreement with Jelurida B.V.,
 * no part of this software, including this file, may be copied, modified,
 * propagated, or distributed except according to the terms contained in the
 * LICENSE.txt file.
 *
 * Removal or modification of this copyright notice is prohibited.
 *
 */

package nxt.http;

import nxt.Constants;
import nxt.Nxt;
import nxt.NxtException;
import nxt.account.Account;
import nxt.account.PublicKeyAnnouncementAppendix;
import nxt.blockchain.Attachment;
import nxt.blockchain.Chain;
import nxt.blockchain.ChainTransactionId;
import nxt.blockchain.ChildChain;
import nxt.blockchain.ChildTransaction;
import nxt.blockchain.ChildTransactionType;
import nxt.blockchain.FxtChain;
import nxt.blockchain.FxtTransactionType;
import nxt.blockchain.Transaction;
import nxt.crypto.Crypto;
import nxt.messaging.EncryptToSelfMessageAppendix;
import nxt.messaging.EncryptedMessageAppendix;
import nxt.messaging.MessageAppendix;
import nxt.messaging.PrunableEncryptedMessageAppendix;
import nxt.messaging.PrunablePlainMessageAppendix;
import nxt.peer.Peers;
import nxt.util.Convert;
import nxt.util.JSON;
import nxt.util.Logger;
import nxt.voting.PhasingAppendix;
import nxt.voting.PhasingParams;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;

import static nxt.http.JSONResponses.FEATURE_NOT_AVAILABLE;
import static nxt.http.JSONResponses.INCORRECT_EC_BLOCK;
import static nxt.http.JSONResponses.MISSING_SECRET_PHRASE;
import static nxt.http.JSONResponses.NOT_ENOUGH_FUNDS;

public abstract class CreateTransaction extends APIServlet.APIRequestHandler {

    private static final String[] commonParameters = new String[]{"secretPhrase", "publicKey", "feeNQT", "feeRateNQTPerFXT", "minBundlerBalanceFXT",
            "deadline", "referencedTransaction", "broadcast", "timestamp",
            "message", "messageIsText", "messageIsPrunable",
            "messageToEncrypt", "messageToEncryptIsText", "encryptedMessageData", "encryptedMessageNonce", "encryptedMessageIsPrunable", "compressMessageToEncrypt",
            "messageToEncryptToSelf", "messageToEncryptToSelfIsText", "encryptToSelfMessageData", "encryptToSelfMessageNonce", "compressMessageToEncryptToSelf",
            "phased", "phasingFinishHeight", "phasingVotingModel", "phasingQuorum", "phasingMinBalance", "phasingHolding", "phasingMinBalanceModel",
            "phasingWhitelisted", "phasingWhitelisted", "phasingWhitelisted",
            "phasingLinkedTransaction", "phasingLinkedTransaction", "phasingLinkedTransaction",
            "phasingHashedSecret", "phasingHashedSecretAlgorithm", "phasingParams", "phasingSubPolls",
            "phasingSenderPropertySetter", "phasingSenderPropertyName",
            "phasingSenderPropertyValue", "phasingRecipientPropertySetter",
            "phasingRecipientPropertyName", "phasingRecipientPropertyValue",
            "phasingExpression",
            "recipientPublicKey",
            "ecBlockId", "ecBlockHeight",
            "voucher"
    };

    private static String[] addCommonParameters(String[] parameters) {
        String[] result = Arrays.copyOf(parameters, parameters.length + commonParameters.length);
        System.arraycopy(commonParameters, 0, result, parameters.length, commonParameters.length);
        return result;
    }

    CreateTransaction(APITag[] apiTags, String... parameters) {
        super(apiTags, addCommonParameters(parameters));
        if (!getAPITags().contains(APITag.CREATE_TRANSACTION)) {
            throw new RuntimeException("CreateTransaction API " + getClass().getName() + " is missing APITag.CREATE_TRANSACTION tag");
        }
    }

    protected CreateTransaction(String fileParameter, APITag[] apiTags, String... parameters) {
        super(fileParameter, apiTags, addCommonParameters(parameters));
        if (!getAPITags().contains(APITag.CREATE_TRANSACTION)) {
            throw new RuntimeException("CreateTransaction API " + getClass().getName() + " is missing APITag.CREATE_TRANSACTION tag");
        }
    }

    protected final JSONStreamAware createTransaction(HttpServletRequest req, Account senderAccount, Attachment attachment)
            throws NxtException {
        return createTransaction(req, senderAccount, 0, 0, attachment, null);
    }

    protected final JSONStreamAware createTransaction(HttpServletRequest req, Account senderAccount, Attachment attachment,
            Chain txChain) throws NxtException {
        return createTransaction(req, senderAccount, 0, 0, attachment, txChain);
    }

    private PhasingAppendix parsePhasing(HttpServletRequest req) throws ParameterException {
        int finishHeight = ParameterParser.getInt(req, "phasingFinishHeight",
                Nxt.getBlockchain().getHeight() + 1,
                Nxt.getBlockchain().getHeight() + Constants.MAX_PHASING_DURATION + 1,
                true);
        JSONObject phasingParamsJson = ParameterParser.getJson(req, "phasingParams");
        PhasingParams phasingParams;
        if (phasingParamsJson != null) {
            phasingParams = new PhasingParams(phasingParamsJson);
        } else {
            phasingParams = ParameterParser.parsePhasingParams(req, "phasing");
        }
        return new PhasingAppendix(finishHeight, phasingParams);
    }

    final JSONStreamAware createTransaction(HttpServletRequest req, Account senderAccount, long recipientId,
                                            long amountNQT, Attachment attachment) throws NxtException {
        return createTransaction(req, senderAccount, recipientId, amountNQT, attachment, null);
    }

    final JSONStreamAware createTransaction(HttpServletRequest req, Account senderAccount, long recipientId,
                                            long amountNQT, Attachment attachment, Chain txChain) throws NxtException {
        return createTransaction(req, senderAccount, recipientId, amountNQT, attachment, txChain, null);
    }

    protected final JSONStreamAware createTransaction(HttpServletRequest req, Account senderAccount, long recipientId,
                                            long amountNQT, Attachment attachment, Chain txChain,
                                            ChainTransactionId referencedTransactionId) throws NxtException {
        if (referencedTransactionId == null) {
            referencedTransactionId = ParameterParser.getChainTransactionId(req, "referencedTransaction");
        }
        String secretPhrase = ParameterParser.getSecretPhrase(req, false);
        boolean isVoucher = "true".equalsIgnoreCase(req.getParameter("voucher"));
        String publicKeyValue = Convert.emptyToNull(req.getParameter("publicKey"));
        boolean broadcast = !"false".equalsIgnoreCase(req.getParameter("broadcast")) && secretPhrase != null && !isVoucher;
        EncryptedMessageAppendix encryptedMessage = null;
        PrunableEncryptedMessageAppendix prunableEncryptedMessage = null;
        if (attachment.getTransactionType().canHaveRecipient() && recipientId != 0) {
            Account recipient = Account.getAccount(recipientId);
            if ("true".equalsIgnoreCase(req.getParameter("encryptedMessageIsPrunable"))) {
                prunableEncryptedMessage = (PrunableEncryptedMessageAppendix) ParameterParser.getEncryptedMessage(req, recipient, true);
            } else {
                encryptedMessage = (EncryptedMessageAppendix) ParameterParser.getEncryptedMessage(req, recipient, false);
            }
        }
        EncryptToSelfMessageAppendix encryptToSelfMessage = ParameterParser.getEncryptToSelfMessage(req);
        MessageAppendix message = null;
        PrunablePlainMessageAppendix prunablePlainMessage = null;
        if ("true".equalsIgnoreCase(req.getParameter("messageIsPrunable"))) {
            prunablePlainMessage = (PrunablePlainMessageAppendix) ParameterParser.getPlainMessage(req, true);
        } else {
            message = (MessageAppendix) ParameterParser.getPlainMessage(req, false);
        }
        PublicKeyAnnouncementAppendix publicKeyAnnouncement = null;
        String recipientPublicKey = Convert.emptyToNull(req.getParameter("recipientPublicKey"));
        if (recipientPublicKey != null) {
            publicKeyAnnouncement = new PublicKeyAnnouncementAppendix(Convert.parseHexString(recipientPublicKey));
        }

        PhasingAppendix phasing = null;
        boolean phased = "true".equalsIgnoreCase(req.getParameter("phased"));
        if (phased) {
            phasing = parsePhasing(req);
        }

        if (secretPhrase == null && publicKeyValue == null) {
            return MISSING_SECRET_PHRASE;
        }

        short deadline = (short)ParameterParser.getInt(req, "deadline", 1, Short.MAX_VALUE, 15);
        long feeNQT = ParameterParser.getLong(req, "feeNQT", -1L, Constants.MAX_BALANCE_NQT, -1L);
        int ecBlockHeight = ParameterParser.getInt(req, "ecBlockHeight", 0, Integer.MAX_VALUE, false);
        long ecBlockId = Convert.parseUnsignedLong(req.getParameter("ecBlockId"));
        if (ecBlockId != 0 && ecBlockId != Nxt.getBlockchain().getBlockIdAtHeight(ecBlockHeight)) {
            return INCORRECT_EC_BLOCK;
        }
        if (ecBlockId == 0 && ecBlockHeight > 0) {
            ecBlockId = Nxt.getBlockchain().getBlockIdAtHeight(ecBlockHeight);
        }
        int timestamp = ParameterParser.getTimestamp(req);
        long feeRateNQTPerFXT = ParameterParser.getLong(req, "feeRateNQTPerFXT", -1L, Constants.MAX_BALANCE_NQT, -1L);
        JSONObject response = new JSONObject();

        // shouldn't try to get publicKey from senderAccount as it may have not been set yet
        byte[] publicKey = secretPhrase != null && !isVoucher ? Crypto.getPublicKey(secretPhrase) : Convert.parseHexString(publicKeyValue);

        // Allow the caller to specify the chain for the transaction instead of using
        // the 'chain' request parameter
        Chain chain;
        if (txChain == null) {
            chain = ParameterParser.getChain(req);
        } else {
            chain = txChain;
        }

        if (feeNQT < 0L && feeRateNQTPerFXT < 0L && chain != FxtChain.FXT) {
            long minBundlerBalanceFXT = ParameterParser.getLong(req, "minBundlerBalanceFXT", 0, Constants.MAX_BALANCE_FXT, false);
            feeRateNQTPerFXT = Peers.getBestBundlerRate(chain, minBundlerBalanceFXT, Peers.getBestBundlerRateWhitelist());
            broadcast = false;
            if (!isVoucher) {
                response.put("bundlerRateNQTPerFXT", String.valueOf(feeRateNQTPerFXT));
            }
        }

        try {
            Transaction.Builder builder = chain.newTransactionBuilder(publicKey, amountNQT, feeNQT, deadline, attachment);
            if (chain instanceof ChildChain) {
                if (!(attachment.getTransactionType() instanceof ChildTransactionType)) {
                    throw new ParameterException(JSONResponses.incorrect("chain",
                            attachment.getTransactionType().getName() + " attachment not allowed for "
                                    + chain.getName() + " chain"));
                }
                builder = ((ChildTransaction.Builder)builder)
                        .referencedTransaction(referencedTransactionId)
                        .feeRateNQTPerFXT(feeRateNQTPerFXT)
                        .appendix(encryptedMessage)
                        .appendix(message)
                        .appendix(publicKeyAnnouncement)
                        .appendix(encryptToSelfMessage)
                        .appendix(phasing);
            } else {
                if (!(attachment.getTransactionType() instanceof FxtTransactionType)) {
                    throw new ParameterException(JSONResponses.incorrect("chain",
                            attachment.getTransactionType().getName() + " attachment not allowed for "
                                    + chain.getName() + " chain"));
                }
                if (referencedTransactionId != null) {
                    return JSONResponses.error("Referenced transactions not allowed for Ardor transactions");
                }
                if (encryptedMessage != null) {
                    return JSONResponses.error("Permanent encrypted message attachments not allowed for Ardor transactions");
                }
                if (message != null) {
                    return JSONResponses.error("Permanent message attachments not allowed for Ardor transactions");
                }
                if (publicKeyAnnouncement != null) {
                    return JSONResponses.error("Public key announcement attachments not allowed for Ardor transactions");
                }
                if (encryptToSelfMessage != null) {
                    return JSONResponses.error("Encrypted to self message attachments not allowed for Ardor transactions");
                }
                if (phasing != null) {
                    return JSONResponses.error("Phasing attachments not allowed for Ardor transactions");
                }
            }
            builder.appendix(prunablePlainMessage)
                    .appendix(prunableEncryptedMessage);
            if (attachment.getTransactionType().canHaveRecipient()) {
                builder.recipientId(recipientId);
            }
            if (ecBlockId != 0) {
                builder.ecBlockId(ecBlockId);
                builder.ecBlockHeight(ecBlockHeight);
            }
            if (timestamp > 0) {
                builder.timestamp(timestamp);
            }
            Transaction transaction = builder.build(secretPhrase, isVoucher);
            try {
                long balance = chain.getBalanceHome().getBalance(senderAccount.getId()).getUnconfirmedBalance();
                if (Math.addExact(amountNQT, transaction.getFee()) > balance) {
                    JSONObject infoJson = new JSONObject();
                    infoJson.put("errorCode", 6);
                    infoJson.put("errorDescription", "Not enough funds");
                    infoJson.put("amount", String.valueOf(amountNQT));
                    infoJson.put("fee", String.valueOf(transaction.getFee()));
                    infoJson.put("balance", String.valueOf(balance));
                    infoJson.put("diff", String.valueOf(Math.subtractExact(Math.addExact(amountNQT, transaction.getFee()), balance)));
                    infoJson.put("chain", chain.getId());
                    return JSON.prepare(infoJson);
                }
            } catch (ArithmeticException e) {
                Logger.logErrorMessage(String.format("amount %d fee %d", amountNQT, transaction.getFee()), e);
                return NOT_ENOUGH_FUNDS;
            }
            JSONObject transactionJSON = JSONData.unconfirmedTransaction(transaction);
            if (isVoucher) {
                transactionJSON.remove("fullHash");
                transactionJSON.put("signature", null);
            }
            response.put("transactionJSON", transactionJSON);
            try {
                response.put("unsignedTransactionBytes", Convert.toHexString(transaction.getUnsignedBytes()));
            } catch (NxtException.NotYetEncryptedException ignore) {}
            if (secretPhrase != null) {
                if (isVoucher) {
                    response.put("signature", Convert.toHexString(transaction.getSignature()));
                    response.put("publicKey", Convert.toHexString(Crypto.getPublicKey(secretPhrase)));
                    response.put("requestType", req.getParameter("requestType"));
                    ParameterParser.parseVoucher(response);
                } else {
                    response.put("fullHash", transactionJSON.get("fullHash"));
                    response.put("transactionBytes", Convert.toHexString(transaction.getBytes()));
                    response.put("signatureHash", transactionJSON.get("signatureHash"));
                }
            }
            if (!isVoucher) {
                response.put("minimumFeeFQT", String.valueOf(transaction.getMinimumFeeFQT()));
            }
            if (broadcast) {
                Nxt.getTransactionProcessor().broadcast(transaction);
                response.put("broadcasted", true);
            } else {
                transaction.validate();
                if (!isVoucher) {
                    response.put("broadcasted", false);
                }
            }
        } catch (NxtException.NotYetEnabledException e) {
            return FEATURE_NOT_AVAILABLE;
        } catch (NxtException.InsufficientBalanceException e) {
            throw e;
        } catch (NxtException.ValidationException e) {
            if (broadcast) {
                response.clear();
            }
            response.put("broadcasted", false);
            JSONData.putException(response, e);
        }
        return response;

    }

    @Override
    protected final boolean requirePost() {
        return true;
    }

    @Override
    protected final boolean allowRequiredBlockParameters() {
        return false;
    }

    @Override
    protected final boolean isChainSpecific() {
        return true;
    }

}
