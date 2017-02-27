(function () {
    angular.module("OfficeManagementSystem")
        .controller("NewRequestController", ["$scope", "$http",
            function ($scope, $http) {
                $scope.requestCredentials = {};

                $scope.requestNameCheck = function () {
                    var nameValue = $scope.requestCredentials.name,
                        regExp = /^[A-Z][a-zA-Z\d]{3,50}$/;

                    if (nameValue){
                        var lookFor = nameValue.search(regExp);

                        if (lookFor == -1){
                            $("#request-name-input-group").addClass("request-name-incorrect");
                            $("#request-name-input-group").addClass("form-control_offset");
                        }else{
                            $("#request-name-input-group").removeClass("request-name-incorrect");
                            $("#request-name-input-group").removeClass("form-control_offset");
                        }

                    }
                };

                $scope.sendRequestCredentials = function () {
                    $http.post("/api/request", $scope.requestCredentials)
                        .then(function (callback) {
                            $scope.name = callback.data.name;
                        }, function (callback) {
                            console.log("Creating request Failure!")
                        })
                }

            }])
})();