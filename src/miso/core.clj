(ns miso.core)

(use 'clojure.java.io)
(use 'server.socket)
(use '[clojure.string :only (join)])

(def ^:dynamic *current-room*)

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

(defn game-handler [in out]
  (binding [*in* (reader in)
            *out* (writer out)]
    (binding [*current-room* (ref (rooms :start))]
      (dosync (commute (@*current-room* :players)
                       conj (make-player "bob")))
      (loop [input (read-line)]
        (client-input-loop input)
        (print-room @*current-room*)
        (if (= input "exit")
          (println "Byeeee")
          (recur (read-line)))))))

(defn game-serv []
  (create-server 8080 game-handler))

(def my-server (game-serv))
