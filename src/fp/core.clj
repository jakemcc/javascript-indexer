(ns fp.core
  (:require [clojure.string :as str]))

(defn starts-function
  [line]
  (.startsWith line "function "))

(defn count-character
  [c s]
  (count (filter #(= c %) s)))

(defn count-open-braces
  [line]
  (count-character \{ line))

(defn count-closing-braces
  [line]
  (count-character \} line))

(defmulti parse-line (fn [line state] (:state state)))

(defmethod parse-line :outside
  [line prev-state]
  (if (starts-function line)
    {:state :inside-fn
     :parens (- (count-open-braces line)
                (count-closing-braces line))
     :code [line]}
    prev-state))

(defmethod parse-line :inside-fn
  [line prev-state]
  (let [prev-parens (:parens prev-state)
        prev-lines (:code prev-state)]
    (assoc prev-state
      :parens (- prev-parens (- (count-closing-braces line)
                                (count-open-braces line)
                                ))
      :code (conj prev-lines line))))

(defn parse-file
  ([lines] (parse-file lines {:state :outside} []))
  ([lines state functions]
     (if (seq lines)
       (let [new-state (parse-line (first lines) state)]
         (if (and (= :inside-fn (:state new-state))
                  (zero? (:parens new-state)))
           (recur (rest lines) {:state :outside} (conj functions (:code new-state)))
           (recur (rest lines) new-state functions)))
       functions)))

(defn read-file
  [file]
  (with-open [rdr (clojure.java.io/reader file)]
    (parse-file (line-seq rdr))))

(defn extract-name
  [js-code]
  (when-let [match (re-find #"function\s+(\w+)" (first js-code))]
    (second match)))

(defn generate-mapping
  [file-path]
  (let [functions (read-file file-path)
        fn-names (map extract-name functions)]
    (zipmap fn-names functions)))

(defn find-files
  [directory x]
  (filter #(.contains (.getName %) x)
          (file-seq (clojure.java.io/file directory))))

(defn map->entries
  [m]
  (mapv (fn [[k v]] {:entry k :definition v}) m))

(defn dictionary
  [title creator entries]
  {:title title
   :creator creator
   :words entries})

(defn generate
  [fj-directory]
  (let [files (find-files fj-directory "js")
        dict (apply merge (map generate-mapping files))
        entries (map->entries dict)]
    (dictionary "Functional Javascript Companion"
                "Jake McCrary"
                entries)))

(defn -main
  "Takes 1 or more directories containing javascript source. Prints dictionary representation of source to screen. Representation is able to be read using read-string."
  [& args]
  (if (>= (count args) 1)
    (prn (apply merge (map generate args)))
    (println "Need to pass at least one directory of javascript source at command line")))

(comment
  (-main "../book-source")
  )
