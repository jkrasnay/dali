(ns dali.layout.matrix
  (:require [dali.layout :as layout]
            [dali.layout.utils :refer [place-by-anchor]]))

(defn- sizes->offsets [sizes]
  (->> sizes
       (partition 2 1)
       (map (fn [[n1 n2]] (+ (/ n1 2) (/ n2 2))))))

(defmethod layout/layout-nodes :matrix
  [_ {{:keys [position columns]} :attrs :as tag} elements bounds-fn]
  (let [bounds-fn     (fn [element] (if (= :_ element) [:rect [0 0] [0 0]] (bounds-fn element)))
        position      (or position (second (bounds-fn (first elements))))
        bounds        (map bounds-fn elements)
        sizes         (zipmap elements (map #(nth % 2) bounds))
        rows          (partition-all columns elements)
        columns       (apply map vector rows)
        row-height    (fn [elements] (apply max (map (comp second sizes) elements)))
        row-heights   (map row-height rows)
        y-positions   (reductions + (second position) (sizes->offsets row-heights))
        column-width  (fn [elements] (apply max (map (comp first sizes) elements)))
        column-widths (map column-width columns)
        x-positions   (reductions + (first position) (sizes->offsets column-widths))]
    ;;(utils/prn-names bounds column-widths x-positions row-heights y-positions)
    (->> (for [y y-positions
           x x-positions]
           [x y])
         (map (fn [e b p]
                (when-not (= e :_)
                  (place-by-anchor e :center p b))) elements bounds)
         (remove nil?))))