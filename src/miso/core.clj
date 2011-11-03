(ns miso.core)

(use 'clojure.java.io)
(use 'server.socket)

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
