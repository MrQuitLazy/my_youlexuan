app.controller("brandController", function($scope, $controller, brandService) {
    // 继承
    $controller("baseController", {
        $scope : $scope
    });

    // 保存
    $scope.save = function() {
        brandService.save($scope.entity).success(function(response) {
            if (response.success) {
                // 重新加载
                $scope.reloadList();
            } else {
                alert(response.message);
            }
        });
    }

    // 查询实体
    $scope.findOne = function(id) {
        brandService.findOne(id).success(function(response) {
            $scope.entity = response;
        });
    }

    // 批量删除
    $scope.dele = function() {
        // 获取选中的复选框
        brandService.dele($scope.selectIds).success(function(response) {
            if (response.success) {
                // 刷新列表
                $scope.reloadList();
                $("#selall").prop("checked",false);
                // 清空选中的id
                $scope.selectIds = [];
            }
        });
    }

    // 防止查询条件为空时，提交null至后台，而查不出结果
    $scope.searchEntity = {};
    $scope.search = function(page, size) {
        // post提交，page、size属性和之前相同，将searchEntity提交至后台@RequestBody对应的属性
        brandService.search(page, size, $scope.searchEntity).success(
            function(response) {
                $scope.list = response.rows;
                $scope.paginationConf.totalItems = response.total;
            });
    }
});
