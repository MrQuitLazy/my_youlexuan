//��½�����
app.service('loginService',function($http){
    //��ȡ��¼������
    this.loginName=function(){
        return $http.get('../login/name.do');
    }
});
