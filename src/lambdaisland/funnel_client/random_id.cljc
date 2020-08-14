(ns lambdaisland.funnel-client.random-id)

(def words ["air" "apple" "arm" "art" "award" "baby" "bag" "ball" "bat" "bath" "beach" "bear" "bed" "bell" "big" "bike"
            "bird" "blue" "boat" "book" "boot" "bowl" "box" "boy" "brain" "brave" "bread" "brown" "brush" "buddy" "bug"
            "bunch" "bus" "cake" "calm" "camp" "can" "candy" "cap" "care" "cat" "chair" "cheek" "clock" "cloud" "clue"
            "coast" "cow" "craft" "cream" "cup" "curve" "cycle" "dance" "dare" "dark" "day" "dear" "dirt" "dish" "disk"
            "dog" "door" "dot" "draw" "dream" "dress" "drop" "due" "dump" "dust" "ear" "earth" "ease" "east" "edge"
            "egg" "entry" "equal" "eye" "face" "fan" "farm" "feed" "feel" "field" "film" "fire" "fish" "fix" "floor"
            "flow" "fly" "focus" "fold" "food" "foot" "form" "front" "fruit" "fuel" "fun" "funny" "gain" "game" "gap"
            "gift" "girl" "give" "glad" "glass" "glove" "go" "goal" "gold" "golf" "good" "grab" "grade" "grand" "grass"
            "great" "green" "guess" "guest" "guide" "habit" "hair" "half" "hall" "hand" "hat" "head" "heart" "heat"
            "heavy" "hello" "high" "hire" "hold" "home" "honey" "hook" "hope" "horse" "host" "hotel" "hour" "house"
            "human" "ice" "idea" "ideal" "image" "iron" "join" "joke" "juice" "jump" "keep" "key" "kid" "kind" "kiss"
            "knee" "lab" "lady" "lake" "land" "laugh" "lay" "layer" "leave" "leg" "level" "life" "lift" "light" "line"
            "link" "lip" "loan" "log" "look" "loss" "love" "low" "luck" "lunch" "mail" "major" "make" "many" "map"
            "mate" "math" "meal" "meat" "meet" "menu" "mess" "metal" "milk" "mind" "mix" "mode" "model" "mom" "month"
            "mood" "most" "mouse" "mouth" "move" "movie" "mud" "music" "nail" "name" "neat" "neck" "nerve" "news"
            "night" "noise" "north" "nose" "note" "novel" "nurse" "offer" "oven" "pace" "pack" "page" "paper" "park"
            "part" "party" "pass" "past" "path" "pause" "peace" "peak" "pen" "phase" "photo" "piano" "pick" "pie"
            "piece" "pin" "pipe" "pizza" "place" "plan" "plant" "plate" "play" "poem" "poet" "point" "pool" "pop" "pot"
            "pound" "power" "press" "prior" "prize" "queen" "quiet" "radio" "rain" "reach" "read" "red" "rice" "ride"
            "ring" "rise" "river" "road" "rock" "roll" "roof" "room" "rope" "rough" "round" "row" "rub" "rule" "run"
            "rush" "safe" "sail" "salad" "sale" "salt" "sand" "scene" "score" "sea" "seat" "self" "sense" "set" "shake"
            "shape" "share" "she" "shift" "shine" "ship" "shirt" "shoe" "shoot" "show" "side" "sign" "silly" "sing"
            "sink" "site" "size" "skill" "skin" "skirt" "sky" "sleep" "slice" "slide" "slip" "smell" "smile" "snow"
            "sock" "soft" "soil" "solid" "son" "song" "sort" "sound" "soup" "south" "space" "spare" "speed" "spell"
            "spend" "spite" "split" "sport" "spot" "spray" "stand" "star" "start" "stay" "steak" "step" "stick" "still"
            "stock" "storm" "story" "strip" "style" "sugar" "sun" "sweet" "swim" "swing" "table" "tale" "talk" "taste"
            "tea" "teach" "team" "tell" "time" "tip" "today" "toe" "tone" "tool" "tooth" "top" "topic" "touch" "tough"
            "tour" "towel" "tower" "town" "track" "trade" "train" "treat" "tree" "trick" "trip" "trust" "truth" "try"
            "tune" "turn" "twist" "two" "uncle" "union" "unit" "upper" "use" "user" "usual" "vast" "view" "visit"
            "voice" "wait" "wake" "walk" "wall" "wash" "watch" "water" "wave" "way" "wear" "web" "week" "weird" "west"
            "wheel" "white" "whole" "will" "win" "wind" "wine" "wing" "wish" "woman" "wood" "word" "work" "world"
            "worth" "wrap" "year" "you" "young" "youth" "berry" "island" "lighthouse"])

(defn rand-id []
  (let [r1 (rand-nth words)
        r2 (rand-nth words)]
    (if (= r1 r2)
      (recur)
      (str r1 "-" r2))))

(comment
  ;; ~ 200k combinations is unique enough for our purposes
  (* (count words)
     (count words))
  ;; => 212521
  )
