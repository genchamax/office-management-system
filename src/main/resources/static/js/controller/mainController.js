/**
 * Created by Max on 22.02.2017.
 */
(function () {
    angular.module("OfficeManagementSystem")
        .controller("MainController", ["$scope", "$http", "$resource", "$cookies",
            function ($scope, $http, $resource, $cookies) {


                var isLoginPage = function () {
                    return ~window.location.href.indexOf("login");
                };

                if (isLoginPage()) {
                    if ($cookies.get("access_token")) {
                        window.location.href = "/test/auth";
                    }
                } else {
                    if ($cookies.get("access_token")) {
                        $http.defaults.headers.common.Authorization =
                            'Bearer ' + $cookies.get("access_token");
                    } else {
                        window.location.href = "/login";
                    }
                }

            }])
})();