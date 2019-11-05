/*
 * Copyright © 2013-2016 The Nxt Core Developers.
 * Copyright © 2016-2019 Jelurida IP B.V.
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
package nxt;

import nxt.util.Convert;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;

public final class Constants {

    public static final boolean isTestnet = Nxt.getBooleanProperty("nxt.isTestnet");
    public static final boolean isOffline = Nxt.getBooleanProperty("nxt.isOffline");
    public static final boolean isLightClient = Nxt.getBooleanProperty("nxt.isLightClient");
    public static final boolean isPermissioned = Nxt.getBooleanProperty("nxt.isPermissioned");
    public static final boolean isAutomatedTest = isTestnet && Nxt.getBooleanProperty("nxt.isAutomatedTest");
    public static final String[] ALL_SECRET_PHRASE_WORDS = {"like", "just", "love", "know", "never", "want", "time",
            "out", "there", "make", "look", "eye", "down", "only", "think", "heart", "back", "then", "into", "about",
            "more", "away", "still", "them", "take", "thing", "even", "through", "long", "always", "world", "too",
            "friend", "tell", "try", "hand", "thought", "over", "here", "other", "need", "smile", "again", "much",
            "cry", "been", "night", "ever", "little", "said", "end", "some", "those", "around", "mind", "people",
            "girl", "leave", "dream", "left", "turn", "myself", "give", "nothing", "really", "off", "before",
            "something", "find", "walk", "wish", "good", "once", "place", "ask", "stop", "keep", "watch", "seem",
            "everything", "wait", "got", "yet", "made", "remember", "start", "alone", "run", "hope", "maybe", "believe",
            "body", "hate", "after", "close", "talk", "stand", "own", "each", "hurt", "help", "home", "god", "soul",
            "new", "many", "two", "inside", "should", "true", "first", "fear", "mean", "better", "play", "another",
            "gone", "change", "use", "wonder", "someone", "hair", "cold", "open", "best", "any", "behind", "happen",
            "water", "dark", "laugh", "stay", "forever", "name", "work", "show", "sky", "break", "came", "deep",
            "door", "put", "black", "together", "upon", "happy", "such", "great", "white", "matter", "fill", "past",
            "please", "burn", "cause", "enough", "touch", "moment", "soon", "voice", "scream", "anything", "stare",
            "sound", "red", "everyone", "hide", "kiss", "truth", "death", "beautiful", "mine", "blood", "broken",
            "very", "pass", "next", "forget", "tree", "wrong", "air", "mother", "understand", "lip", "hit", "wall",
            "memory", "sleep", "free", "high", "realize", "school", "might", "skin", "sweet", "perfect", "blue", "kill",
            "breath", "dance", "against", "fly", "between", "grow", "strong", "under", "listen", "bring", "sometimes",
            "speak", "pull", "person", "become", "family", "begin", "ground", "real", "small", "father", "sure", "feet",
            "rest", "young", "finally", "land", "across", "today", "different", "guy", "line", "fire", "reason",
            "reach", "second", "slowly", "write", "eat", "smell", "mouth", "step", "learn", "three", "floor", "promise",
            "breathe", "darkness", "push", "earth", "guess", "save", "song", "above", "along", "both", "color", "house",
            "almost", "sorry", "anymore", "brother", "okay", "dear", "game", "fade", "already", "apart", "warm",
            "beauty", "heard", "notice", "question", "shine", "began", "piece", "whole", "shadow", "secret", "street",
            "within", "finger", "point", "morning", "whisper", "child", "moon", "green", "story", "glass", "kid",
            "silence", "since", "soft", "yourself", "empty", "shall", "angel", "answer", "baby", "bright", "dad",
            "path", "worry", "hour", "drop", "follow", "power", "war", "half", "flow", "heaven", "act", "chance",
            "fact", "least", "tired", "children", "near", "quite", "afraid", "rise", "sea", "taste", "window", "cover",
            "nice", "trust", "lot", "sad", "cool", "force", "peace", "return", "blind", "easy", "ready", "roll", "rose",
            "drive", "held", "music", "beneath", "hang", "mom", "paint", "emotion", "quiet", "clear", "cloud", "few",
            "pretty", "bird", "outside", "paper", "picture", "front", "rock", "simple", "anyone", "meant", "reality",
            "road", "sense", "waste", "bit", "leaf", "thank", "happiness", "meet", "men", "smoke", "truly", "decide",
            "self", "age", "book", "form", "alive", "carry", "escape", "damn", "instead", "able", "ice", "minute",
            "throw", "catch", "leg", "ring", "course", "goodbye", "lead", "poem", "sick", "corner", "desire", "known",
            "problem", "remind", "shoulder", "suppose", "toward", "wave", "drink", "jump", "woman", "pretend", "sister",
            "week", "human", "joy", "crack", "grey", "pray", "surprise", "dry", "knee", "less", "search", "bleed",
            "caught", "clean", "embrace", "future", "king", "son", "sorrow", "chest", "hug", "remain", "sat", "worth",
            "blow", "daddy", "final", "parent", "tight", "also", "create", "lonely", "safe", "cross", "dress", "evil",
            "silent", "bone", "fate", "perhaps", "anger", "class", "scar", "snow", "tiny", "tonight", "continue",
            "control", "dog", "edge", "mirror", "month", "suddenly", "comfort", "given", "loud", "quickly", "gaze",
            "plan", "rush", "stone", "town", "battle", "ignore", "spirit", "stood", "stupid", "yours", "brown", "build",
            "dust", "hey", "kept", "pay", "phone", "twist", "although", "ball", "beyond", "hidden", "nose", "taken",
            "fail", "float", "pure", "somehow", "wash", "wrap", "angry", "cheek", "creature", "forgotten", "heat",
            "rip", "single", "space", "special", "weak", "whatever", "yell", "anyway", "blame", "job", "choose",
            "country", "curse", "drift", "echo", "figure", "grew", "laughter", "neck", "suffer", "worse", "yeah",
            "disappear", "foot", "forward", "knife", "mess", "somewhere", "stomach", "storm", "beg", "idea", "lift",
            "offer", "breeze", "field", "five", "often", "simply", "stuck", "win", "allow", "confuse", "enjoy",
            "except", "flower", "seek", "strength", "calm", "grin", "gun", "heavy", "hill", "large", "ocean", "shoe",
            "sigh", "straight", "summer", "tongue", "accept", "crazy", "everyday", "exist", "grass", "mistake", "sent",
            "shut", "surround", "table", "ache", "brain", "destroy", "heal", "nature", "shout", "sign", "stain",
            "choice", "doubt", "glance", "glow", "mountain", "queen", "stranger", "throat", "tomorrow", "city",
            "either", "fish", "flame", "rather", "shape", "spin", "spread", "ash", "distance", "finish", "image",
            "imagine", "important", "nobody", "shatter", "warmth", "became", "feed", "flesh", "funny", "lust", "shirt",
            "trouble", "yellow", "attention", "bare", "bite", "money", "protect", "amaze", "appear", "born", "choke",
            "completely", "daughter", "fresh", "friendship", "gentle", "probably", "six", "deserve", "expect", "grab",
            "middle", "nightmare", "river", "thousand", "weight", "worst", "wound", "barely", "bottle", "cream",
            "regret", "relationship", "stick", "test", "crush", "endless", "fault", "itself", "rule", "spill", "art",
            "circle", "join", "kick", "mask", "master", "passion", "quick", "raise", "smooth", "unless", "wander",
            "actually", "broke", "chair", "deal", "favorite", "gift", "note", "number", "sweat", "box", "chill",
            "clothes", "lady", "mark", "park", "poor", "sadness", "tie", "animal", "belong", "brush", "consume", "dawn",
            "forest", "innocent", "pen", "pride", "stream", "thick", "clay", "complete", "count", "draw", "faith",
            "press", "silver", "struggle", "surface", "taught", "teach", "wet", "bless", "chase", "climb", "enter",
            "letter", "melt", "metal", "movie", "stretch", "swing", "vision", "wife", "beside", "crash", "forgot",
            "guide", "haunt", "joke", "knock", "plant", "pour", "prove", "reveal", "steal", "stuff", "trip", "wood",
            "wrist", "bother", "bottom", "crawl", "crowd", "fix", "forgive", "frown", "grace", "loose", "lucky",
            "party", "release", "surely", "survive", "teacher", "gently", "grip", "speed", "suicide", "travel", "treat",
            "vein", "written", "cage", "chain", "conversation", "date", "enemy", "however", "interest", "million",
            "page", "pink", "proud", "sway", "themselves", "winter", "church", "cruel", "cup", "demon", "experience",
            "freedom", "pair", "pop", "purpose", "respect", "shoot", "softly", "state", "strange", "bar", "birth",
            "curl", "dirt", "excuse", "lord", "lovely", "monster", "order", "pack", "pants", "pool", "scene", "seven",
            "shame", "slide", "ugly", "among", "blade", "blonde", "closet", "creek", "deny", "drug", "eternity", "gain",
            "grade", "handle", "key", "linger", "pale", "prepare", "swallow", "swim", "tremble", "wheel", "won", "cast",
            "cigarette", "claim", "college", "direction", "dirty", "gather", "ghost", "hundred", "loss", "lung",
            "orange", "present", "swear", "swirl", "twice", "wild", "bitter", "blanket", "doctor", "everywhere",
            "flash", "grown", "knowledge", "numb", "pressure", "radio", "repeat", "ruin", "spend", "unknown", "buy",
            "clock", "devil", "early", "false", "fantasy", "pound", "precious", "refuse", "sheet", "teeth", "welcome",
            "add", "ahead", "block", "bury", "caress", "content", "depth", "despite", "distant", "marry", "purple",
            "threw", "whenever", "bomb", "dull", "easily", "grasp", "hospital", "innocence", "normal", "receive",
            "reply", "rhyme", "shade", "someday", "sword", "toe", "visit", "asleep", "bought", "center", "consider",
            "flat", "hero", "history", "ink", "insane", "muscle", "mystery", "pocket", "reflection", "shove",
            "silently", "smart", "soldier", "spot", "stress", "train", "type", "view", "whether", "bus", "energy",
            "explain", "holy", "hunger", "inch", "magic", "mix", "noise", "nowhere", "prayer", "presence", "shock",
            "snap", "spider", "study", "thunder", "trail", "admit", "agree", "bag", "bang", "bound", "butterfly",
            "cute", "exactly", "explode", "familiar", "fold", "further", "pierce", "reflect", "scent", "selfish",
            "sharp", "sink", "spring", "stumble", "universe", "weep", "women", "wonderful", "action", "ancient",
            "attempt", "avoid", "birthday", "branch", "chocolate", "core", "depress", "drunk", "especially", "focus",
            "fruit", "honest", "match", "palm", "perfectly", "pillow", "pity", "poison", "roar", "shift", "slightly",
            "thump", "truck", "tune", "twenty", "unable", "wipe", "wrote", "coat", "constant", "dinner", "drove",
            "egg", "eternal", "flight", "flood", "frame", "freak", "gasp", "glad", "hollow", "motion", "peer",
            "plastic", "root", "screen", "season", "sting", "strike", "team", "unlike", "victim", "volume", "warn",
            "weird", "attack", "await", "awake", "built", "charm", "crave", "despair", "fought", "grant", "grief",
            "horse", "limit", "message", "ripple", "sanity", "scatter", "serve", "split", "string", "trick", "annoy",
            "blur", "boat", "brave", "clearly", "cling", "connect", "fist", "forth", "imagination", "iron", "jock",
            "judge", "lesson", "milk", "misery", "nail", "naked", "ourselves", "poet", "possible", "princess", "sail",
            "size", "snake", "society", "stroke", "torture", "toss", "trace", "wise", "bloom", "bullet", "cell",
            "check", "cost", "darling", "during", "footstep", "fragile", "hallway", "hardly", "horizon", "invisible",
            "journey", "midnight", "mud", "nod", "pause", "relax", "shiver", "sudden", "value", "youth", "abuse",
            "admire", "blink", "breast", "bruise", "constantly", "couple", "creep", "curve", "difference", "dumb",
            "emptiness", "gotta", "honor", "plain", "planet", "recall", "rub", "ship", "slam", "soar", "somebody",
            "tightly", "weather", "adore", "approach", "bond", "bread", "burst", "candle", "coffee", "cousin", "crime",
            "desert", "flutter", "frozen", "grand", "heel", "hello", "language", "level", "movement", "pleasure",
            "powerful", "random", "rhythm", "settle", "silly", "slap", "sort", "spoken", "steel", "threaten", "tumble",
            "upset", "aside", "awkward", "bee", "blank", "board", "button", "card", "carefully", "complain", "crap",
            "deeply", "discover", "drag", "dread", "effort", "entire", "fairy", "giant", "gotten", "greet", "illusion",
            "jeans", "leap", "liquid", "march", "mend", "nervous", "nine", "replace", "rope", "spine", "stole",
            "terror", "accident", "apple", "balance", "boom", "childhood", "collect", "demand", "depression",
            "eventually", "faint", "glare", "goal", "group", "honey", "kitchen", "laid", "limb", "machine", "mere",
            "mold", "murder", "nerve", "painful", "poetry", "prince", "rabbit", "shelter", "shore", "shower", "soothe",
            "stair", "steady", "sunlight", "tangle", "tease", "treasure", "uncle", "begun", "bliss", "canvas", "cheer",
            "claw", "clutch", "commit", "crimson", "crystal", "delight", "doll", "existence", "express", "fog",
            "football", "gay", "goose", "guard", "hatred", "illuminate", "mass", "math", "mourn", "rich", "rough",
            "skip", "stir", "student", "style", "support", "thorn", "tough", "yard", "yearn", "yesterday", "advice",
            "appreciate", "autumn", "bank", "beam", "bowl", "capture", "carve", "collapse", "confusion", "creation",
            "dove", "feather", "girlfriend", "glory", "government", "harsh", "hop", "inner", "loser", "moonlight",
            "neighbor", "neither", "peach", "pig", "praise", "screw", "shield", "shimmer", "sneak", "stab", "subject",
            "throughout", "thrown", "tower", "twirl", "wow", "army", "arrive", "bathroom", "bump", "cease", "cookie",
            "couch", "courage", "dim", "guilt", "howl", "hum", "husband", "insult", "led", "lunch", "mock", "mostly",
            "natural", "nearly", "needle", "nerd", "peaceful", "perfection", "pile", "price", "remove", "roam",
            "sanctuary", "serious", "shiny", "shook", "sob", "stolen", "tap", "vain", "void", "warrior", "wrinkle",
            "affection", "apologize", "blossom", "bounce", "bridge", "cheap", "crumble", "decision", "descend",
            "desperately", "dig", "dot", "flip", "frighten", "heartbeat", "huge", "lazy", "lick", "odd", "opinion",
            "process", "puzzle", "quietly", "retreat", "score", "sentence", "separate", "situation", "skill", "soak",
            "square", "stray", "taint", "task", "tide", "underneath", "veil", "whistle", "anywhere", "bedroom", "bid",
            "bloody", "burden", "careful", "compare", "concern", "curtain", "decay", "defeat", "describe", "double",
            "dreamer", "driver", "dwell", "evening", "flare", "flicker", "grandma", "guitar", "harm", "horrible",
            "hungry", "indeed", "lace", "melody", "monkey", "nation", "object", "obviously", "rainbow", "salt",
            "scratch", "shown", "shy", "stage", "stun", "third", "tickle", "useless", "weakness", "worship",
            "worthless", "afternoon", "beard", "boyfriend", "bubble", "busy", "certain", "chin", "concrete", "desk",
            "diamond", "doom", "drawn", "due", "felicity", "freeze", "frost", "garden", "glide", "harmony", "hopefully",
            "hunt", "jealous", "lightning", "mama", "mercy", "peel", "physical", "position", "pulse", "punch", "quit",
            "rant", "respond", "salty", "sane", "satisfy", "savior", "sheep", "slept", "social", "sport", "tuck",
            "utter", "valley", "wolf", "aim", "alas", "alter", "arrow", "awaken", "beaten", "belief", "brand",
            "ceiling", "cheese", "clue", "confidence", "connection", "daily", "disguise", "eager", "erase", "essence",
            "everytime", "expression", "fan", "flag", "flirt", "foul", "fur", "giggle", "glorious", "ignorance", "law",
            "lifeless", "measure", "mighty", "muse", "north", "opposite", "paradise", "patience", "patient", "pencil",
            "petal", "plate", "ponder", "possibly", "practice", "slice", "spell", "stock", "strife", "strip",
            "suffocate", "suit", "tender", "tool", "trade", "velvet", "verse", "waist", "witch", "aunt", "bench",
            "bold", "cap", "certainly", "click", "companion", "creator", "dart", "delicate", "determine", "dish",
            "dragon", "drama", "drum", "dude", "everybody", "feast", "forehead", "former", "fright", "fully", "gas",
            "hook", "hurl", "invite", "juice", "manage", "moral", "possess", "raw", "rebel", "royal", "scale", "scary",
            "several", "slight", "stubborn", "swell", "talent", "tea", "terrible", "thread", "torment", "trickle",
            "usually", "vast", "violence", "weave", "acid", "agony", "ashamed", "awe", "belly", "blend", "blush",
            "character", "cheat", "common", "company", "coward", "creak", "danger", "deadly", "defense", "define",
            "depend", "desperate", "destination", "dew", "duck", "dusty", "embarrass", "engine", "example", "explore",
            "foe", "freely", "frustrate", "generation", "glove", "guilty", "health", "hurry", "idiot", "impossible",
            "inhale", "jaw", "kingdom", "mention", "mist", "moan", "mumble", "mutter", "observe", "ode", "pathetic",
            "pattern", "pie", "prefer", "puff", "rape", "rare", "revenge", "rude", "scrape", "spiral", "squeeze",
            "strain", "sunset", "suspend", "sympathy", "thigh", "throne", "total", "unseen", "weapon", "weary"};

    public static final String COMPRESSED_SECRET_PHRASE_WORDS;

    static {
        String words = String.join(",", ALL_SECRET_PHRASE_WORDS);
        COMPRESSED_SECRET_PHRASE_WORDS = Convert.toHexString(Convert.compress(words.getBytes(StandardCharsets.UTF_8)));
    }

    static {
        if (isPermissioned) {
            try {
                Class.forName("com.jelurida.blockchain.authentication.BlockchainRoleMapper");
            } catch (ClassNotFoundException e) {
                throw new ExceptionInInitializerError("BlockchainRoleMapper class required for a permissioned blockchain");
            }
        }
    }

    public static final String ROLE_MAPPING_ACCOUNT_PROPERTY = "ROLE_MAPPING";

    public static final String ACCOUNT_PREFIX = "ARDOR";
    public static final int MAX_NUMBER_OF_FXT_TRANSACTIONS = 10;
    public static final int MAX_NUMBER_OF_CHILD_TRANSACTIONS = 100;
    public static final int MAX_CHILDBLOCK_PAYLOAD_LENGTH = 128 * 1024;
    public static final long EPOCH_BEGINNING;

    static {
        try {
            EPOCH_BEGINNING = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z")
                    .parse(isTestnet ? "2017-12-26 14:00:00 +0000" : "2018-01-01 00:00:00 +0000").getTime();
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public static final String customLoginWarning = Nxt.getStringProperty("nxt.customLoginWarning", null, false, "UTF-8");

    public static final long MAX_BALANCE_FXT = 1000000000;
    public static final long ONE_FXT = 100000000;
    public static final BigInteger ONE_FXT_BIG_INTEGER = BigInteger.valueOf(ONE_FXT);
    public static final long MAX_BALANCE_NQT = MAX_BALANCE_FXT * ONE_FXT;
    public static final int BLOCK_TIME = 60;
    public static final int TESTNET_ACCELERATION = 6;
    public static final long INITIAL_BASE_TARGET = BigInteger.valueOf(2).pow(63).divide(BigInteger.valueOf(BLOCK_TIME * MAX_BALANCE_FXT)).longValue(); //153722867;
    public static final long MAX_BASE_TARGET = INITIAL_BASE_TARGET * (isTestnet ? MAX_BALANCE_FXT : 50);
    public static final long MIN_BASE_TARGET = INITIAL_BASE_TARGET * 9 / 10;
    public static final int MIN_BLOCKTIME_DELTA = 7;
    public static final int MAX_BLOCKTIME_DELTA = 7;
    public static final int BASE_TARGET_GAMMA = 64;
    public static final int MAX_ROLLBACK = Math.max(Nxt.getIntProperty("nxt.maxRollback"), 720);
    public static final int GUARANTEED_BALANCE_CONFIRMATIONS = isTestnet ? Nxt.getIntProperty("nxt.testnetGuaranteedBalanceConfirmations", 1440) : 1440;
    public static final int LEASING_DELAY = isTestnet ? Nxt.getIntProperty("nxt.testnetLeasingDelay", 1440) : 1440;
    public static final long MIN_FORGING_BALANCE_FQT = 1000 * ONE_FXT;

    public static final int MAX_TIMEDRIFT = 15; // allow up to 15 s clock difference
    public static final int FORGING_DELAY = Math.min(MAX_TIMEDRIFT - 1, Nxt.getIntProperty("nxt.forgingDelay"));
    public static final int FORGING_SPEEDUP = Nxt.getIntProperty("nxt.forgingSpeedup");
    public static final int DEFAULT_NUMBER_OF_FORK_CONFIRMATIONS = Nxt.getIntProperty(Constants.isTestnet
            ? "nxt.testnetNumberOfForkConfirmations" : "nxt.numberOfForkConfirmations");

    public static final int BATCH_COMMIT_SIZE = Nxt.getIntProperty("nxt.batchCommitSize", Integer.MAX_VALUE);

    public static final byte MAX_PHASING_VOTE_TRANSACTIONS = 10;
    public static final byte MAX_PHASING_WHITELIST_SIZE = 10;
    public static final byte MAX_PHASING_LINKED_TRANSACTIONS = 10;
    public static final int MAX_PHASING_DURATION = 14 * 1440;
    public static final int MAX_PHASING_REVEALED_SECRETS_COUNT = 10;
    public static final int MAX_PHASING_REVEALED_SECRET_LENGTH = 100;
    public static final int MAX_PHASING_COMPOSITE_VOTE_EXPRESSION_LENGTH = 1000;
    public static final int MAX_PHASING_COMPOSITE_VOTE_SUBPOLL_NAME_LENGTH = 10;
    public static final int MAX_PHASING_COMPOSITE_VOTE_VARIABLES_COUNT = 20;
    public static final int MAX_PHASING_COMPOSITE_VOTE_LITERALS_COUNT = 30;

    public static final int MAX_ALIAS_URI_LENGTH = 1000;
    public static final int MAX_ALIAS_LENGTH = 100;

    public static final int MAX_ARBITRARY_MESSAGE_LENGTH = 160;
    public static final int MAX_ENCRYPTED_MESSAGE_LENGTH = 160 + 16;

    public static final int MAX_PRUNABLE_MESSAGE_LENGTH = 42 * 1024;
    public static final int MAX_PRUNABLE_ENCRYPTED_MESSAGE_LENGTH = 42 * 1024;

    public static final int MIN_PRUNABLE_LIFETIME = isTestnet ? 1440 * 60 : 14 * 1440 * 60;
    public static final int MAX_PRUNABLE_LIFETIME;
    public static final boolean ENABLE_PRUNING;

    static {
        int maxPrunableLifetime = Nxt.getIntProperty("nxt.maxPrunableLifetime");
        ENABLE_PRUNING = maxPrunableLifetime >= 0;
        MAX_PRUNABLE_LIFETIME = ENABLE_PRUNING ? Math.max(maxPrunableLifetime, MIN_PRUNABLE_LIFETIME) : Integer.MAX_VALUE;
    }

    public static final boolean INCLUDE_EXPIRED_PRUNABLE = Nxt.getBooleanProperty("nxt.includeExpiredPrunable");

    public static final int MAX_ACCOUNT_NAME_LENGTH = 100;
    public static final int MAX_ACCOUNT_DESCRIPTION_LENGTH = 1000;

    public static final int MAX_ACCOUNT_PROPERTY_NAME_LENGTH = 32;
    public static final int MAX_ACCOUNT_PROPERTY_VALUE_LENGTH = 160;
    public static final int MAX_ASSET_PROPERTY_NAME_LENGTH = 32;
    public static final int MAX_ASSET_PROPERTY_VALUE_LENGTH = 160;

    public static final long MAX_ASSET_QUANTITY_QNT = 1000000000L * 100000000L;
    public static final int MIN_ASSET_NAME_LENGTH = 3;
    public static final int MAX_ASSET_NAME_LENGTH = 10;
    public static final int MAX_ASSET_DESCRIPTION_LENGTH = 1000;
    public static final int MAX_SINGLETON_ASSET_DESCRIPTION_LENGTH = 160;
    public static final int MAX_DIVIDEND_PAYMENT_ROLLBACK = 1441;
    public static final int MIN_DIVIDEND_PAYMENT_INTERVAL = isTestnet ? 3 : 60;

    public static final int MAX_POLL_NAME_LENGTH = 100;
    public static final int MAX_POLL_DESCRIPTION_LENGTH = 1000;
    public static final int MAX_POLL_OPTION_LENGTH = 100;
    public static final int MAX_POLL_OPTION_COUNT = 100;
    public static final int MAX_POLL_DURATION = 14 * 1440;

    public static final byte MIN_VOTE_VALUE = -92;
    public static final byte MAX_VOTE_VALUE = 92;
    public static final byte NO_VOTE_VALUE = Byte.MIN_VALUE;

    public static final int MAX_DGS_LISTING_QUANTITY = 1000000000;
    public static final int MAX_DGS_LISTING_NAME_LENGTH = 100;
    public static final int MAX_DGS_LISTING_DESCRIPTION_LENGTH = 1000;
    public static final int MAX_DGS_LISTING_TAGS_LENGTH = 100;
    public static final int MAX_DGS_GOODS_LENGTH = 1000;

    public static final int MIN_CURRENCY_NAME_LENGTH = 3;
    public static final int MAX_CURRENCY_NAME_LENGTH = 10;
    public static final int MIN_CURRENCY_CODE_LENGTH = 3;
    public static final int MAX_CURRENCY_CODE_LENGTH = 5;
    public static final int MAX_CURRENCY_DESCRIPTION_LENGTH = 1000;
    public static final long MAX_CURRENCY_TOTAL_SUPPLY = 1000000000L * 100000000L;
    public static final int MAX_MINTING_RATIO = 10000; // per mint units not more than 0.01% of total supply
    public static final byte MIN_NUMBER_OF_SHUFFLING_PARTICIPANTS = 3;
    public static final byte MAX_NUMBER_OF_SHUFFLING_PARTICIPANTS = 30; // max possible at current block payload limit is 51
    public static final short MAX_SHUFFLING_REGISTRATION_PERIOD = (short) 1440 * 7;
    public static final short SHUFFLING_PROCESSING_DEADLINE = (short) (isTestnet ? 10 : 100);
    public static final int SHUFFLER_EXPIRATION_DELAY_BLOCKS = isAutomatedTest ? 1 : 720;

    public static final int MAX_TAGGED_DATA_NAME_LENGTH = 100;
    public static final int MAX_TAGGED_DATA_DESCRIPTION_LENGTH = 1000;
    public static final int MAX_TAGGED_DATA_TAGS_LENGTH = 100;
    public static final int MAX_TAGGED_DATA_TYPE_LENGTH = 100;
    public static final int MAX_TAGGED_DATA_CHANNEL_LENGTH = 100;
    public static final int MAX_TAGGED_DATA_FILENAME_LENGTH = 100;
    public static final int MAX_TAGGED_DATA_DATA_LENGTH = 42 * 1024;

    public static final int MAX_CONTRACT_NAME_LENGTH = 32;
    public static final int MAX_CONTRACT_PARAMS_LENGTH = 160;

    public static final int MAX_REFERENCED_TRANSACTION_TIMESPAN = 60 * 1440 * 60;
    public static final int CHECKSUM_BLOCK_1 = Constants.isTestnet ? 17000 : 6000;
    public static final int CHECKSUM_BLOCK_2 = Constants.isTestnet ? 230000 : 221000;
    public static final int LIGHT_CONTRACTS_BLOCK = Constants.isTestnet ? Constants.isAutomatedTest ? 0 : 341500 : 543000;
    public static final int ASSET_PROPERTIES_BLOCK = Constants.isTestnet ? Constants.isAutomatedTest ? 0 : 455000 : 543000;
    public static final int MPG_BLOCK = Constants.isTestnet ? Constants.isAutomatedTest ? 1 : 455000 : 543000;
    public static final int CHECKSUM_BLOCK_3 = Constants.isTestnet ? 974000 : 545555;
    public static final int CHECKSUM_BLOCK_4 = Constants.isTestnet ? 1738000 : 695000;
    public static final int CHECKSUM_BLOCK_5 = Constants.isTestnet ? 3214500 : 983000;
    public static final int MISSING_TX_SENDER_BLOCK = Constants.isTestnet ? Constants.isAutomatedTest ? 0 : 3250000 : Integer.MAX_VALUE;

    public static final int LAST_CHECKSUM_BLOCK = CHECKSUM_BLOCK_5;

    public static final int LAST_KNOWN_BLOCK = CHECKSUM_BLOCK_5;
    public static final long LAST_KNOWN_BLOCK_ID = Convert.parseUnsignedLong(isTestnet ? "5657621390974142748" : "11295165462625039807");

    public static final int[] MIN_VERSION = new int[]{2, 2, 1};
    public static final int[] MIN_PROXY_VERSION = new int[]{2, 2, 1};

    public static final long UNCONFIRMED_POOL_DEPOSIT_FQT = 10 * ONE_FXT;

    public static final boolean correctInvalidFees = Nxt.getBooleanProperty("nxt.correctInvalidFees");
    public static final int minBundlerBalanceFXT = Nxt.getIntProperty("nxt.minBundlerBalanceFXT");
    public static final int minBundlerFeeLimitFXT = Nxt.getIntProperty("nxt.minBundlerFeeLimitFXT");

    public static final String ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyz";
    public static final String ALLOWED_CURRENCY_CODE_LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    public static final long BURN_ACCOUNT_ID = Convert.parseAccountId("ARDOR-Q9KZ-74XD-WERK-CV6GB");

    public static final boolean DISABLE_FULL_TEXT_SEARCH = Nxt.getBooleanProperty("nxt.disableFullTextSearch");
    public static final boolean DISABLE_METADATA_DETECTION = Nxt.getBooleanProperty("nxt.disableMetadataDetection");

    private Constants() {
    } // never

}
