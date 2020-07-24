app.controller("brandController", function($scope, $controller, brandService) {
    // �̳�
    $controller("baseController", {
        $scope : $scope
    });

    // ����
    $scope.save = function() {
        brandService.save($scope.entity).success(function(response) {
            if (response.success) {
                // ���¼���
                $scope.reloadList();
            } else {
                alert(response.message);
            }
        });
    }

    // ��ѯʵ��
    $scope.findOne = function(id) {
        brandService.findOne(id).success(function(response) {
            $scope.entity = response;
        });
    }

    // ����ɾ��
    $scope.dele = function() {
        // ��ȡѡ�еĸ�ѡ��
        brandService.dele($scope.selectIds).success(function(response) {
            if (response.success) {
                // ˢ���б�
                $scope.reloadList();
                $("#selall").prop("checked",false);
                // ���ѡ�е�id
                $scope.selectIds = [];
            }
        });
    }

    // ��ֹ��ѯ����Ϊ��ʱ���ύnull����̨�����鲻�����
    $scope.searchEntity = {};
    $scope.search = function(page, size) {
        // post�ύ��page��size���Ժ�֮ǰ��ͬ����searchEntity�ύ����̨@RequestBody��Ӧ������
        brandService.search(page, size, $scope.searchEntity).success(
            function(response) {
                $scope.list = response.rows;
                $scope.paginationConf.totalItems = response.total;
            });
    }
});
