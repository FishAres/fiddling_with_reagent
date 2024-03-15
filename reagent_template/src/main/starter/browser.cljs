(ns starter.browser
  (:require [reagent.core :as r]
            [reagent.dom :as rd]))


(defn button [component-state f text]
  [:button {:on-click
            #(swap! component-state update-in [:count] f)} text])

(defn counter []
  (let [component-state (r/atom {:count 0})]
    (fn []
      [:div
       [:div {:style {:color "white"
                      :width "10rem"
                      :margin "10px"
                      :border "10px"
                      :backgroundColor "grey"
                      :fontSize "28px"}}
        (get @component-state :count)]
       [button component-state inc "Increment"]
       [button component-state dec "Decrement"]])))

(defn app []
  [:div "Meow"
   [counter]])


;; start is called by init and after code reloading finishes
(defn ^:dev/after-load start []
  (rd/render [app] (js/document.getElementById "app")))

(defn init []
  ;; init is called ONCE when the page loads
  ;; this is called in the index.html and must be exported
  ;; so it is available even in :advanced release builds
  (js/console.log "init")
  (start))

;; this is called before any code is reloaded
(defn ^:dev/before-load stop []
  (js/console.log "stop"))
