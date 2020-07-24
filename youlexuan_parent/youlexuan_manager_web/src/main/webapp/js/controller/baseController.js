app.controller("baseController", function($scope) {
    // 分页控件配置
    $scope.paginationConf = {
        currentPage : 1,
        totalItems : 10,
        itemsPerPage : 5,
        perPageOptions : [ 5, 10, 20, 30 ],
        onChange : function() {
            // 切换页码，重新加载
            $scope.reloadList();
        }
    };

    // 刷新列表
    $scope.reloadList = function() {
        $scope.search($scope.paginationConf.currentPage,
            $scope.paginationConf.itemsPerPage);
    }

    // 选中的ID集合
    $scope.selectIds = [];
    // 更新复选
    $scope.updateSelection = function($event, id) {
        // 如果是被选中,则增加到数组
        if ($event.target.checked) {
            $scope.selectIds.push(id);
        } else {
            var idx = $scope.selectIds.indexOf(id);
            // 删除
            $scope.selectIds.splice(idx, 1);
            //全选按钮，取消
            $("#selall").prop("checked",false);
        }
    }

    // 全选
    $scope.selectAll = function($event) {
        var state = $event.target.checked;
        $(".eachbox").each(function(idx, obj) {
            obj.checked = state;
            var id = parseInt($(obj).parent().next().text());
            if (state) {
                $scope.selectIds.push(id);
            } else {
                var idx = $scope.selectIds.indexOf(id);
                $scope.selectIds.splice(idx, 1);
            }
        });
    }

//提取json字符串数据中某个属性，返回拼接字符串 逗号分隔
    $scope.jsonToString=function(jsonString,key){
        var json=JSON.parse(jsonString);//将json字符串转换为json对象
        var value="";
        for(var i=0;i<json.length;i++){
            if(i>0){
                value+=","
            }
            value+=json[i][key];
        }
        return value;
    }

});
