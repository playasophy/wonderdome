(ns playasophy.wonderdome.geometry.graph
  "Functions for working with layouts as a graph structure.")

;;         11
;;        /   \
;;       /     \
;;      6-------2
;;     /|\     /|\
;;    / |  \  / | \
;;  10  |   1   |  7
;;    \ |  /|\  | /
;;     \|/  |  \|/
;;      5---4---3
;;      |  / \  |
;;      |/     \|
;;      9       8


(def edges-by-strip
  [[[:v1 :v2 50] [:v2 :v6 62] [:v6 :v11 64] [:v11 :v2 64]]
   [[:v1 :v3 50] [:v3 :v2 62] [:v2 :v7 64] [:v7 :v3 64]]
   [[:v1 :v4 50] [:v4 :v3 62] [:v3 :v8 64] [:v8 :v4 64]]
   [[:v1 :v5 50] [:v5 :v4 62] [:v4 :v9 64] [:v9 :v5 64]]
   [[:v1 :v6 50] [:v6 :v5 62] [:v5 :v10 64] [:v10 :v6 64]]])


(def edges
  (into
    {}
    (map
      (fn [[source sink strip start end]]
        [(keyword (str (name source) \. (name sink)))
         {::source source
          ::sink sink
          ::strip strip
          ::start start
          ::end end}]))
    [[:v1 :v2 0 0 50]
     [:v2 :v6 0 50 112]
     [:v6 :v11 0 112 176]
     [:v11 :v2 0 176 240]
     [:v1 :v3 1 0 50]
     [:v3 :v2 1 50 112]
     [:v2 :v7 1 112 176]
     [:v7 :v3 1 176 240]
     [:v1 :v4 2 0 50]
     [:v4 :v3 2 50 112]
     [:v3 :v8 2 112 176]
     [:v8 :v4 2 176 240]
     [:v1 :v5 3 0 50]
     [:v5 :v4 3 50 112]
     [:v4 :v9 3 112 176]
     [:v9 :v5 3 176 240]
     [:v1 :v6 4 0 50]
     [:v6 :v5 4 50 112]
     [:v5 :v10 4 112 176]
     [:v10 :v6 4 176 240]]))


(defn strip-index->edge-offset
  "Determines which edge a pixel belongs on and what its offset in the edge is."
  [strip pixel]
  (when-let [e (->> edges
                    (filter #(= strip (::strip (val %))))
                    (filter #(and (<= (::start (val %)) pixel)
                                  (< pixel (::end (val %)))))
                    (first))]
    {:graph/edge (key e)
     :graph/offset (- pixel (::start (val e)))}))
