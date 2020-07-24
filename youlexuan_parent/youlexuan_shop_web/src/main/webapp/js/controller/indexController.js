app.controller('indexController' ,function($scope, indexService){
    //读取当前登录人
    $scope.showLoginName=function(){
        indexService.loginName().success(
            function(response){
                $scope.loginName=response.loginName;
            }
        );
    }
});
