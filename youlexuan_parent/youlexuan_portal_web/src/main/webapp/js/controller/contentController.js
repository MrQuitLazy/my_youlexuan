//广告控制层（运营商后台）
app.controller("contentController", function($scope, contentService) {

    $scope.contentList = [];// 广告集合

    $scope.findByCategoryId = function(categoryId) {

        contentService.findByCategoryId(categoryId).success(
            function(response) {
                $scope.contentList[categoryId] = response;
            });
    }
    $scope.toSearch=function () {
        if($scope.keywords!=null && $scope.keywords!=""){
            location.href="http://localhost:9007/search.html#?keywords="+$scope.keywords;
        }
    }
});
