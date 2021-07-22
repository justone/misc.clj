(ns malli
  (:require
    [malli.core :as m]
    ))

#_(m/validate int? "1")
#_(m/validate int? 1)


(def Age
  [:and
   {:title "Age"
    :description "It's an age"
    :json-schema/example 20}
   int? [:> 18]])

(m/properties Age)

#_(m/validate Age 19)
