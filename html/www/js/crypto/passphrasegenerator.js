/******************************************************************************
 * Copyright © 2013-2016 The Nxt Core Developers.                             *
 * Copyright © 2016-2019 Jelurida IP B.V.                                     *
 *                                                                            *
 * See the LICENSE.txt file at the top-level directory of this distribution   *
 * for licensing information.                                                 *
 *                                                                            *
 * Unless otherwise agreed in a custom licensing agreement with Jelurida B.V.,*
 * no part of this software, including this file, may be copied, modified,    *
 * propagated, or distributed except according to the terms contained in the  *
 * LICENSE.txt file.                                                          *
 *                                                                            *
 * Removal or modification of this copyright notice is prohibited.            *
 *                                                                            *
 ******************************************************************************/

/**
 * @depends {../3rdparty/jquery-2.1.0.js}
 */

var PassPhraseGenerator = {
    passPhrase: "",

	generatePassPhrase: function(container) {
        var $container = $(container);
		$container.find(".account_phrase_generator_steps").hide();
		$container.find("textarea").val("");

		var crypto = window.crypto || window.msCrypto;
		if (crypto) {
			$container.find(".step_2").show();
			$("#account_phrase_generator_start").show();
			$("#account_phrase_generator_stop").hide();

			var bits = 128;
			var random = new Uint32Array(bits / 32);
			crypto.getRandomValues(random);
			var words = NRS.constants.SECRET_WORDS;
			var n = words.length;
			var	phraseWords = [];
			var	x, w1, w2, w3;

			for (var i=0; i < random.length; i++) {
				x = random[i];
				w1 = x % n;
				w2 = (((x / n) >> 0) + w1) % n;
				w3 = (((((x / n) >> 0) / n) >> 0) + w2) % n;

				phraseWords.push(words[w1]);
				phraseWords.push(words[w2]);
				phraseWords.push(words[w3]);
			}

			this.passPhrase = phraseWords.join(" ");
			crypto.getRandomValues(random);
			$container.find(".step_2 textarea").val(this.passPhrase).prop("readonly", true);
			$container.find("#step_2_account").val(NRS.getAccountId(this.passPhrase, true));
			setTimeout(function () {
				$("#account_phrase_generator_start").hide();
                $("#confirm_passphrase_warning").prop('checked', false);
                $("#confirm_passphrase_warning_container").css("background-color", "");
				$("#account_phrase_generator_stop").fadeIn("slow");
				$("#custom_passphrase_link").show();
			}, 500);
		} else {
			$container.find(".step_2 textarea").val($.t("unavailable")).prop("readonly", true);
			alert($.t("error_encryption_browser_support"));
		}
	},

	reset: function() {
		this.passPhrase = "";
	}

};