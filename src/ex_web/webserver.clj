(ns ex-web.webserver
  (:require [clojure.pprint :refer [pprint]]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [compojure.handler :as handler]
            [ring.adapter.jetty :as jetty]
            [hiccup.core :refer :all]
            [hiccup.page :refer :all]
            [hiccup.middleware :refer [wrap-base-url]]
            [ex-web.database :as db]))

(defn index-page
  [kitten-part]
  (html5 {:lang "en"}
         [:head
          [:title "What's New Pussycat?"]
          [:link {:rel "stylesheet"
                  :href "//netdna.bootstrapcdn.com/bootstrap/3.0.2/css/bootstrap.min.css"}]]
         [:body
          [:nav.navbar.navbar-default {:role "navigation"}
           [:div.navbar-header
            [:div.navbar-brand "What's New Pussycat?"]]]
          [:div#main.container
           kitten-part
           [:div "The time is "
            [:span.label.label-default (db/current-time)]]]]))

(defn create-kittens [n]
  (loop [cnt n acc []]
    (if (zero? cnt)
      acc
      (recur (dec cnt) (cons [:img.thumbnail {:src (str "http://placekitten.com/200/3" (+ 10 cnt))
                                              :alt "Kitty"}] acc)))))

(defroutes main-routes
  (GET "/" [] (index-page (create-kittens 1)))

  (GET "/kittens/:number" [] (index-page (create-kittens 10)))

  (route/resources "/")
  (route/not-found "Page not found"))

(defn log-time [filter-chain-fn]
 (fn [request]
   (println (format "URI is: %s" (:uri request)))
   (let [response (filter-chain-fn request)]
     (println (format "Time is: %s" (. System (nanoTime))))
     response)))

(def app
  (-> #'main-routes
      handler/site
      wrap-base-url
      (log-time)))
