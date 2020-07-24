app.service("brandService", function($http) {
    // ���桢�޸�
    this.save = function(entity) {
        var methodName = 'add'; // ��������
        if (entity.id != null) { // �����ID
            methodName = 'update'; // ��ִ���޸ķ���
        }
        return $http.post('../brand/' + methodName + '.do', entity);
    }

    // ��ѯ����ʵ��
    this.findOne = function(id) {
        return $http.get('../brand/findOne.do?id=' + id);
    }

    // ����ɾ��
    this.dele = function(ids) {
        // ��ȡѡ�еĸ�ѡ��
        return $http.get('../brand/delete.do?ids=' + ids);
    }

    // ��ѯ
    this.search = function(page, size, searchEntity) {
        // post�ύ��page��size���Ժ�֮ǰ��ͬ����searchEntity�ύ����̨@RequestBody��Ӧ������
        return $http.post('../brand/search.do?page=' + page + '&size=' + size,
            searchEntity);
    }
    //�����б�����
    this.selectOptionList=function(){
        return $http.get('../brand/selectOptionList.do');
    }
});
