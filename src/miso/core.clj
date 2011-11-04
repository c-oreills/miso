(ns miso.core)

(use 'clojure.java.io)
(use 'server.socket)
(use '[clojure.string :only (join)])

(defn move [dir]
  (println (format "moving %swards" dir)))

(defn serv-loop [input]
  (cond
    (and (empty? (rest input))
         (some #{(first input)} "12346789")) (move (first input)),
    true (println (format "wtf is '%s'?" input)),
    ))

(defn game-serv []
  (letfn
    [(game [in out]
           (binding [*in* (reader in)
                     *out* (writer out)]
             (loop [input (read-line)]
               (serv-loop input)
               (if (= input "exit")
                 (println "Byeeee")
                 (recur (read-line))))))]
    (create-server 8080 game)))

(def my-server (game-serv))

(def make-vector (comp vec repeat))

(defn make-room [x y]
  (hash-map
    ; Rooms are vertically stacked vectors of horizontal rows
    :space (make-vector y
                        (make-vector x \.)),
    :players (),
    ))

(defn get-cell [room x y]
  (get-in (room :space) [y x]))

(defn print-room [room]
  (print
    (join "\n"
          (for [row (room :space)]
            (join " " row)))))

;(def *rooms* (hash-map
(def rooms (hash-map
             :start (make-room 10 6),
             :next (make-room 5 5),))

;(def *players* (hash-map))

(defn make-player [name]
  (let [start-room (rooms :start)]
    (assoc start-room :players
           (conj (start-room :players)
                 (hash-map
                   :name name,
                   :x 0,
                   :y 0,
                   :room :start,)))))

(make-player "bob")
