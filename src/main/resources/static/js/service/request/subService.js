(function () {
    angular.module("OfficeManagementSystem")
        .service("SubService", ["$http",
            function ($http) {
                var service = {};

                service.getSubRequests = function (id) {
                    return $http.get("/api/request/"+id+"/subrequests")
                        .then(function (callback) {
                            callback.isError = false;
                            angular.forEach(callback.data, function (obj) {
                                obj["showEdit"] = false;
                                obj["estimate"] = transformEstimate(obj["estimate"]);
                            });
                            return callback;
                        }, function (callback) {
                            callback.isError = true;
                            return callback;
                        })
                };

                service.getStatuses = function () {
                    return $http.get("/api/statuses")
                        .then(function (callback) {
                            callback.isError = false;
                            return callback;
                        }, function (callback) {
                            callback.isError = true;
                            return callback;
                        })
                };

                service.getPriorities = function () {
                    return $http.get("/api/priorities")
                        .then(function (callback) {
                            callback.isError = false;
                            return callback;
                        }, function (callback) {
                            callback.isError = true;
                            return callback;
                        })
                };

                service.addSubRequest = function (sub, parent) {
                    return $http.post("/api/request/"+parent+"/subrequests", sub)
                        .then(function (callback) {
                            callback.isError = false;
                            callback.data["showEdit"] = false;
                            callback.data["estimate"] = transformEstimate(callback.data["estimate"]);
                            callback.sub = callback.data;
                            return callback;
                        }, function (callback) {
                            callback.isError = true;
                            return callback;
                        })
                };

                service.deleteSubRequest = function (id, parent) {
                    return $http.delete("/api/request/"+parent+"/subrequests/"+id)
                        .then(function (callback) {
                            callback.isError = false;
                            return callback;
                        }, function (callback) {
                            callback.isError = true;
                            return callback;
                        })
                };

                service.updateSubRequest = function (id, sub) {

                };

                service.finishSubRequest = function (id) {

                };

                var transformEstimate = function (timestamp) {
                    return new Date(timestamp);
                };

                return service;
            }])
})();