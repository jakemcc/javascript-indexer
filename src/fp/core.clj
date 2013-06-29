(ns fp.core
  (:require [clojure.string :as str]))

(defn starts-function
  [line]
  (.startsWith line "function "))

(defn count-character
  [c s]
  (count (filter #(= c %) s)))

(defn num-open-brace
  [line]
  (count-character \{ line))

(defn num-close-brace
  [line]
  (count-character \} line))

(defmulti parse-line (fn [line state] (:state state)))

(defmethod parse-line :outside
  [line prev-state]
  (if (starts-function line)
    {:state :inside-fn
     :parens (- (num-open-brace line)
                (num-close-brace line))
     :code [line]}
    prev-state))

(defmethod parse-line :inside-fn
  [line prev-state]
  (let [prev-parens (:parens prev-state)
        prev-lines (:code prev-state)]
    (assoc prev-state
      :parens (- prev-parens (- (num-close-brace line)
                                (num-open-brace line)
                                ))
      :code (conj prev-lines line))))

(defn parse-file
  ([lines] (parse-file lines {:state :outside} []))
  ([lines state functions]
     (if (seq lines)
       (let [new-state (parse-line (first lines) state)]
         (if
             (and (= :inside-fn (:state new-state))
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

