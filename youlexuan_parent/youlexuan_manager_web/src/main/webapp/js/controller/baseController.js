app.controller("baseController", function($scope) {
    // ��ҳ�ؼ�����
    $scope.paginationConf = {
        currentPage : 1,
        totalItems : 10,
        itemsPerPage : 5,
        perPageOptions : [ 5, 10, 20, 30 ],
        onChange : function() {
            // �л�ҳ�룬���¼���
            $scope.reloadList();
        }
    };

    // ˢ���б�
    $scope.reloadList = function() {
        $scope.search($scope.paginationConf.currentPage,
            $scope.paginationConf.itemsPerPage);
    }

    // ѡ�е�ID����
    $scope.selectIds = [];
    // ���¸�ѡ
    $scope.updateSelection = function($event, id) {
        // ����Ǳ�ѡ��,�����ӵ�����
        if ($event.target.checked) {
            $scope.selectIds.push(id);
        } else {
            var idx = $scope.selectIds.indexOf(id);
            // ɾ��
            $scope.selectIds.splice(idx, 1);
            //ȫѡ��ť��ȡ��
            $("#selall").prop("checked",false);
        }
    }

    // ȫѡ
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

//��ȡjson�ַ���������ĳ�����ԣ�����ƴ���ַ��� ���ŷָ�
    $scope.jsonToString=function(jsonString,key){
        var json=JSON.parse(jsonString);//��json�ַ���ת��Ϊjson����
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
