(ns kanban-new.core
  (:require
   [reagent.core :as r]
   [reagent.dom :as d]))

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

(defn AutoFocusInput [props]
  (r/create-class {:displayName "AutoFocusInput"
                   :component-did-mount (fn [component]
                                          (.focus (d/dom-node component)))
                   :reagent-render (fn [props]
                                     [:input props])}))

(defn- update-title [card-cur title]
  (swap! card-cur assoc :title title))

(defn- stop-editing [card-cur]
  (swap! card-cur dissoc :editing))

(defn- start-editing [card-cur]
  (swap! card-cur assoc :editing true))

(defn Editable [el cursor]
  (let [{:keys [editing title]} @cursor]
    (if editing
      [el {:className "editing"}
       [AutoFocusInput {:type "text"
                        :value title
                        :on-change #(update-title cursor (.. % -target -value))
                        :on-blur #(stop-editing cursor)
                        :on-key-press #(if (= (.-charCode %) 13)
                                         (stop-editing cursor))}]]
      [el {:on-click #(start-editing cursor)} title])))

(defn Card [cursor]
  [Editable :div.card cursor])

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
     ^{:key "title"} [Editable :h2 col-cur]
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

;; -------------------------
;; Initialize app

(defn mount-root []
  (d/render [Board app-state] (.getElementById js/document "app")))

(defn ^:export init! []
  (mount-root))
