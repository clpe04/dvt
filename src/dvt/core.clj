(ns dvt.core)

(defmacro check
  "Takes a function with the entry to check as it only argument or a name and a
  function, in the format 'function :as name' and returns a function ready to be
  used as a validation check"
  ([function _ name]
  `^{:name ~name}
   (fn [entry#]
     (~function entry#)))
  ([function]
     `(check ~function :as (:name (meta (var ~function))))))

(defn on-field
  "Takes one or more checks and rewrites them to work on a value associated with a specific key.
  This function return the rewriten function, if only one was given, or a list containig all the
  rewritten checks, if more than one was given"
  ([field check]
     ^{:name (:name (meta check))}
     (fn [entry] (check (field entry))))
  ([field check & checks]
     (conj (map #(on-field field %) checks) (on-field field check))))

(defn validator
  "Returns a validator function, which takes a entry and runs all checks on the given entry,
  and returns the entry with the return values of the checks added as meta data."
  [& checks]
  (let [checks (flatten checks)]
    (fn [entry]
      (with-meta entry
        (assoc (meta entry)
              :dvt (zipmap (map #(keyword (:name (meta %))) checks)
                            (map #(% entry) checks)))))))

(defn valid?
  "Test used to check whether a given validation succeeded"
  [check-name entry]
  (true? (check-name (:dvt (meta entry)))))

(defn invalid?
  "Test used to check whether a given validation failed"
  [check-name entry]
  (false? (check-name (:dvt (meta entry)))))