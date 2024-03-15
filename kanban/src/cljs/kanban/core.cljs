(ns kanban.core
  (:require [reagent.core :as r]))

(enable-console-print!)

(def app-state (r/atom {:columns  [{:id (random-uuid)
                                    :title "Manus"
                                    :cards [{:id (random-uuid)
                                             :title "Oh hai thur" :editing false}
                                            {:id (random-uuid)
                                             :title "Meow" :editing false}]}
                                   {:id (random-uuid)
                                    :title "Biggus"
                                    :cards [{:id (random-uuid)
                                             :title "Dickus?" :editing false}
                                            {:id (random-uuid)
                                             :title "Oh no :(" :editing false}]}]}))
;; r/cursor navigates a tree, like `get-in`

(defn- update-title [card-cur title]
  (swap! card-cur assoc :title title))

(defn- stop-editing [card-cur]
  (swap! card-cur dissoc :editing))

(defn- start-editing [card-cur]
  (swap! card-cur assoc :editing true))

(defn Card [card-cur]
  (let [{:keys [editing title]} @card-cur]
    (if editing
      [:div.card.editing
       [:input {:type "text"
                :value title
                :autoFocus true
                :on-change #(update-title card-cur (.. % -target -value))
                :on-blur #(stop-editing card-cur)
                :on-key-press #(if (= (.-charCode %) 13)
                                 (stop-editing card-cur))}]]
      [:div.card {:on-click #(start-editing card-cur)} title])))

(defn- add-new-card [col-cur]
  (swap! col-cur update :cards conj {:id (random-uuid)
                                     :title ""
                                     :editing true}))

(defn- add-new-column [board]
  (swap! board update :columns conj {:id (random-uuid)
                                     :title ""
                                     :cards [] ;; !!!
                                     :editing true}))

(defn NewCard [col-cur]
  [:div.new-card
   {:on-click #(add-new-card col-cur)}
   "+ add new card"])

(defn NewColumn [board]
  [:div.new-column
   {:on-click #(add-new-column board)}
   "+ add new column"])

;; The props correspond to the :columns key from the app-state
(defn Column [col-cur]
  (let [{:keys [title cards editing]} @col-cur]
    [:div.column
     (if editing
       [:input {:type "text"
                :value title
                :autoFocus true
                :on-change #(update-title col-cur (.. % -target -value))
                :on-blur #(stop-editing col-cur)}]
       [:h2 title])
     (map-indexed (fn [idx card]
                    (let [card-cur (r/cursor col-cur [:cards idx])]
                      ^{:key (:id card)} ; ^ adds to metadata
                      [Card card-cur])) cards)
     ^{:key "new"} [NewCard col-cur]]))

(defn Board [board]
  [:div.board
   (map-indexed (fn [idx col]
                  (let [col-cur (r/cursor board [:columns idx])]
                    ^{:key (:id col)} [Column col-cur])) (:columns @board))
   [NewColumn board]])

(r/render [Board app-state] (js/document.getElementById "app"))
 