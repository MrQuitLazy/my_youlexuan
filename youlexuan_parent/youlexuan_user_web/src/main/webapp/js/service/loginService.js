//�����
app.service('loginService',function($http){
    //��ȡ�б����ݰ󶨵�����
    this.showName=function(){
        return $http.get('../login/name.do');
    }
});
