(ns miso.core)

(use 'clojure.java.io)
(use 'server.socket)
(use '[clojure.string :only (join)])

(def ^:dynamic *current-room*)
(def ^:dynamic *input-streams* (ref {}))
(def ^:dynamic *output-streams* (ref {}))

(defn move [dir]
  (println (format "moving %swards" dir)))

(def make-vector (comp vec repeat))

(defn make-room [x y]
  (hash-map
    ; Rooms are vertically stacked vectors of horizontal rows
    :space (ref (make-vector y
                        (make-vector x \.))),
    :players (ref ()),
    ))

(defn get-cell [room x y]
  (get-in (room :space) [y x]))

(defn print-room [room]
  (println
    (join "\n"
          (for [row @(room :space)]
            (join " " row)))))

(def rooms (ref (hash-map
                  :start (make-room 10 6),
                  :next (make-room 5 5),)))

(defn make-player [name]
  (hash-map
    :name name,
    :x 0,
    :y 0,
    ))

(defn client-input-loop [input]
  (cond
    (and (empty? (rest input))
         (some #{(first input)} "12346789")) (move (first input)),
    true (println (format "wtf is '%s'?" input)),
    ))

(defn get-player-name [in]
  (binding [*in* (reader in)]
    (read-line)))

(defn initial-setup [player-name]
  (dosync
    (commute (@*current-room* :players)
             conj (make-player player-name))
    (commute *input-streams*
             assoc player-name *in*)
    (commute *output-streams*
             assoc player-name *out*)))

(defn game-handler
  ([in out player-name]
   (binding [*in* (reader in)
             *out* (writer out)
             *current-room* (ref (rooms :start))]
     (initial-setup player-name)
     (loop [input (read-line)]
       (client-input-loop input)
       (print-room @*current-room*)
       (if (= input "exit")
         (println "Byeeee")
         (recur (read-line))))))
  ([in out] (game-handler in out (get-player-name in))))

(defn game-serv []
  (create-server 8080 game-handler))

;(def my-server (game-serv))

(defn send-output [stream-name & lines]
  (binding [*out* (@*output-streams* stream-name)]
    (doall (map println lines))
    (flush)))

;(defn send-input [stream-name & lines]
;  (binding [*out* (@*input-streams* stream-name)]
;    (map println lines)
;    (flush)))
