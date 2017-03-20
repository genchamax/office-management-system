(function () {
    angular.module("OfficeManagementSystem")
        .controller("RequestListByUserController", ["$scope", "$location", "$http", "PersonService", "RequestService",
            function ($scope, $location, $http, PersonService, RequestService) {

                $scope.selectedManager = undefined;
                $scope.selectedUser = undefined;
                $scope.managers = [];
                $scope.users = [];

                var requestDetails = "/request/";
                var currentUser = JSON.parse(localStorage.getItem("currentUser"));
                $scope.personType = "";
                $scope.pageSize = 10;
                $scope.requests = {};
                $scope.priorities = [{priorityId: 4, name: 'ALL'},
                    {priorityId: 1, name: 'HIGH'},
                    {priorityId: 2, name: 'NORMAL'},
                    {priorityId: 3, name: 'LOW'}]; // TODO need controller for priorities
                $scope.maxSize = 5;
                $scope.totalItems = 0;
                $scope.currentPage = 1;
                $scope.selectedPriority = $scope.priorities[0];
                $scope.assignedMessage = '';
                $scope.selectedRequest = -1;
                $scope.placeholder = '';


                $scope.assigned = false;
                var path = $location.path();

                if (path=="/request/assigned"){
                    $scope.assigned = true;
                    $scope.personType = "Employee";
                    $scope.placeholder = "Find assigned requests by manager name";

                    $scope.updateManager = function() {
                        if($scope.selectedManager.length >= 2) {
                            console.log($scope.selectedManager);
                            $http({
                                method: 'GET',
                                url: '/api/person/managers/' +  $scope.selectedManager +
                                '?page=' +  $scope.currentPage + '&size=' + $scope.pageSize
                            }).then(function successCallback(response) {
                                $scope.managers = response.data;
                                console.log($scope.managers);
                            }, function errorCallback(response) {
                            });
                        }
                    };

                    $scope.pageChanged = function() {
                        if ($scope.selectedManager!=undefined)
                            $http({
                                method: 'GET',
                                url: '/api/request/list/assigned/' +  $scope.selectedManager.id
                            }).then(function (response) {
                                $scope.requests = [];
                                $scope.requests = response.data.data;
                                $scope.totalItems = response.data.totalElements;
                            }, function errorCallback(response) {
                            });
                    };

                    $scope.requestDetails = function (requestId) {
                        window.location = requestDetails + requestId;
                    };

                    $scope.getTotalPage = function(){
                        return $scope.totalItems;
                    };

                    $scope.getTotalPage(); //
                    $scope.pageChanged(1); // get first page

                } else {

                    $scope.personType = "Manager";
                    $scope.placeholder = "Find all requests by user name";
                    $scope.updateUser = function() {
                        if($scope.selectedUser.length >= 2) {
                            console.log($scope.selectedUser);
                            $http({
                                method: 'GET',
                                url: '/api/person/users/' +  $scope.selectedUser +
                                '?page=' +  $scope.currentPage + '&size=' + $scope.pageSize
                            }).then(function successCallback(response) {
                                $scope.users = response.data;
                                console.log($scope.users);
                            }, function errorCallback(response) {
                            });
                        }
                    };

                    $scope.pageChanged = function() {
                        if ($scope.selectedUser!=undefined)
                            $http({
                                method: 'GET',
                                url: '/api/request/list/user/' +  $scope.selectedUser.id
                            }).then(function (response) {
                                $scope.requests = [];
                                $scope.requests = response.data.data;
                                $scope.totalItems = response.data.totalElements;
                            }, function errorCallback(response) {
                            });
                    };

                    $scope.requestDetails = function (requestId) {
                        window.location = requestDetails + requestId;
                    };

                    $scope.getTotalPage = function(){
                        return $scope.totalItems;
                    };

                    $scope.getTotalPage(); //
                    $scope.pageChanged(1); // get first page

                    $scope.requestUpdate = function(requestId) {
                        window.location = requestDetails + requestId + '/update';
                    };
                }


                $scope.requestDetails = function (requestId) {
                    window.location = requestDetails + requestId;
                };

                $scope.isUndefined = function (thing) {
                    return (typeof thing === "undefined");
                };

                $scope.isAdmin = function (thing) {
                    return currentUser.role === 'ROLE_ADMINISTRATOR';
                };

                $scope.assignToMe = function (requestId) {
                    return PersonService.assign(requestId, currentUser.id)
                        .then(function (response) {
                            $scope.assignedMessage = response.data.message;
                        }, function (response) {
                            $scope.assignedMessage = response.data.errors
                                .map(function (e) {
                                    return e.detail
                                })
                                .join('. ');
                        });
                };

                $scope.assignToSmb = function () {
                    return PersonService.assign($scope.request.id, $scope.selectedManager.id)
                        .then(function (response) {
                            $scope.assignedMessage = response.data.message;
                        }, function (response) {
                            $scope.assignedMessage = response.data.errors
                                .map(function (e) {
                                    return e.detail
                                })
                                .join('. ');
                        });
                };

                $scope.requestDelete = function(requestId) {
                    swal({
                            title: "Are you sure?",
                            text: "Do you really want to cancel this request",
                            type: "warning",
                            showCancelButton: true,
                            confirmButtonColor: "#DD6B55",
                            confirmButtonText: "Yes, cancel it!",
                            closeOnConfirm: false
                        },
                        function(){
                            RequestService.cancelRequest(requestId)
                                .then(function (callback) {
                                    $scope.requests = callback.data;
                                }, function (error) {
                                    swal("Cancel Failure!", error.data.errors[0].detail, "error");
                                    console.log(error);
                                });

                            swal("Request canceled!", "", "success");
                            window.setTimeout(function(){
                                location.reload();}, 2000)
                        });
                };



                $scope.selectRequest = function (requestId) {
                    $scope.selectedRequest = requestId;
                };

                $scope.goToRequestDetailsPage = function (requestId) {
                    $scope.goToUrl("/request/" + requestId + "/details");
                };

                $scope.notifyAboutExpiringEstimateTime = function() {
                    return RequestService.notifyAboutExpiringEstimateTime();
                };
            }])
})();
