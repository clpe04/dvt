(ns dvt.test.core
  (:use [dvt.core])
  (:use [clojure.test]))

(defn full-name-test
  [entry]
  (and
   (= "Test" (:first-name entry))
   (= "Testing" (:last-name entry))))

(defn over-18
  [age]
  (>= age 18))

(defn is-age
  [valid-age age-to-check]
  (= valid-age age-to-check))

(defn valid-zip
  [zip-code]
  (let [zip (str zip-code)]
    (and (every? #(Character/isDigit %)  zip) (= 4 (count zip)))))

(def validate-person
  (validator
   (check full-name-test)
   (on-field :first-name
             (check #(= "Test" %) :as :named-test))
   (on-field :age
             (check over-18)
             (check (partial is-age 20) :as :is-age-20))))

(def validate-address
  (validator
   (on-field :zip-code
             (check valid-zip))))

(def person
  {:first-name "Test" :last-name "Testing" :age 26})

(def person-with-zip
  (assoc person :zip-code 2760))

(deftest check-test
  (testing "Creating a validation check with existing function"
    (let [test-check (check over-18)]
      (is true (fn? test-check))
      (is (= "over-18" (str (:name (meta test-check)))))
      (is true (test-check 18))))
  (testing "Creating a validation check with existing function associated with a new name"
    (let [test-check (check over-18 :as :check-age)]
      (is true (fn? test-check))
      (is (= :check-age (:name (meta test-check))))
      (is true (test-check 18))))
  (testing "Creating a validation check with an anonymous function"
    (let [test-check (check #(= "Test" %) :as "check-value")]
      (is true (fn? test-check))
      (is (= "check-value" (:name (meta test-check))))
      (is true (test-check "Test"))))
  (testing "Creating a validation check with an function, which takes ore than one argument"
    (let [test-check (check (partial is-age 20) :as :is-age-20)]
      (is true (fn? test-check))
      (is (= :is-age-20 (:name (meta test-check))))
      (is true (test-check 20)))))

(deftest on-field-test
  (testing "Creating on-field function with a single check function"
    (let [test-on-field (on-field :age (check over-18))]
      (is true (fn? test-on-field))
      (is (= "over-18" (str (:name (meta test-on-field)))))
      (is true (test-on-field {:age 18}))))
  (testing "Creating on-field function with two functions"
    (let [test-on-field (on-field :age
                                  (check over-18)
                                  (check (partial is-age 20) :as :is-age-20))]
      (is true (coll? test-on-field)))))

(deftest validator-test
  (testing "Creating a validator with a single check on a field"
    (let [test-validator (validator
                          (on-field :age
                                    (check over-18)))
          test-dvt-meta (:dvt (meta test-validator))
          test-entry-meta (:dvt (meta (test-validator person)))]
      (is true (fn? test-validator))
      (is true (coll? (:checks test-dvt-meta)))
      (is (= :over-18 (first (:checks test-dvt-meta))))
      (is true (:over-18 test-entry-meta))))
  (testing "Creating a validator with a two checks on a field"
    (let [test-validator (validator
                          (on-field :age
                                    (check over-18)
                                    (check (partial is-age 20) :as :is-age-20)))
          test-dvt-meta (:dvt (meta test-validator))
          test-entry-meta (:dvt (meta (test-validator person)))]
      (is true (fn? test-validator))
      (is true (coll? (:checks test-dvt-meta)))
      (is (= 2 (count (:checks test-dvt-meta))))
      (are [x y] (= x y)
           true (:over-18 test-entry-meta)
           false (:is-age-20 test-entry-meta))))
  (testing "Creating a validator with three checks split on two fields"
    (let [test-validator (validator
                          (on-field :first-name
                                    (check #(= "Test" %) :as :named-test))
                          (on-field :age
                                    (check over-18)
                                    (check (partial is-age 20) :as :is-age-20)))
          test-dvt-meta (:dvt (meta test-validator))
          test-entry-meta (:dvt (meta (test-validator person)))]
      (is true (fn? test-validator))
      (is true (coll? (:checks test-dvt-meta)))
      (is (= 3 (count (:checks test-dvt-meta))))
      (are [x y] (= x y)
           true (:named-test test-entry-meta)
           true (:over-18 test-entry-meta)
           false (:is-age-20 test-entry-meta))))
  (testing "Creating a validator with a single check on the whole entry"
    (let [test-validator (validator
                          (check full-name-test))
          test-dvt-meta (:dvt (meta test-validator))
          test-entry-meta (:dvt (meta (test-validator person)))]
      (is true (fn? test-validator))
      (is true (coll? (:checks test-dvt-meta)))
      (is (= :full-name-test (first (:checks test-dvt-meta))))
      (is true (:full-name-test test-entry-meta))))
  (testing "Creating a validator with checks split on two fields and a check on the whole entry"
    (let [test-validator (validator
                          (on-field :first-name
                                    (check #(= "Test" %) :as :named-test))
                          (on-field :age
                                    (check over-18)
                                    (check (partial is-age 20) :as :is-age-20))
                          (check full-name-test))
          test-dvt-meta (:dvt (meta test-validator))
          test-entry-meta (:dvt (meta (test-validator person)))]
      (is true (fn? test-validator))
      (is true (coll? (:checks test-dvt-meta)))
      (is (= 4 (count (:checks test-dvt-meta))))
      (are [x y] (= x y)
           true (:named-test test-entry-meta)
           true (:over-18 test-entry-meta)
           false (:is-age-20 test-entry-meta)
           true (:full-name-test test-entry-meta))))
  (testing "Validating an entry with two different validators"
    (let [test-entry-meta (:dvt (meta (validate-address (validate-person person-with-zip))))]
      (are [x y] (= x y)
           true (:named-test test-entry-meta)
           true (:over-18 test-entry-meta)
           false (:is-age-20 test-entry-meta)
           true (:full-name-test test-entry-meta)
           true (:valid-zip test-entry-meta)))))
  
(deftest checks-test
  (testing "Getting list of checks on validator"
    (let [test-checks (checks validate-person)]
      (is true (coll? test-checks))
      (is (= 4 (count test-checks)))))
  (testing "Getting list of checks on validated entry"
    (let [test-checks (checks (validate-person person))]
      (is true (coll? test-checks))
      (is (= 4 (count test-checks)))))
  (testing "Getting list of checks on a entry which have been validated by two different validators"
    (let [test-checks (checks (validate-address (validate-person person-with-zip)))]
      (is true (coll? test-checks))
      (is (= 5 (count test-checks))))))

(deftest valid-test
  (testing "Check if a given validation succeeded"
    (let [test-entry (validate-person person)]
      (is true (valid? :over-18 test-entry))))
  (testing "Check if a given validation succeeded - but actually failed"
    (let [test-entry (validate-person person)]
      (is (false? (valid? :is-age-20 test-entry))))))

(deftest invalid-test
  (testing "Check if a given validation failed"
    (let [test-entry (validate-person person)]
      (is true (invalid? :is-age-20 test-entry))))
  (testing "Check if a given validation failed - but actually succeeded"
    (let [test-entry (validate-person person)]
      (is (false? (invalid? :over-18 test-entry))))))