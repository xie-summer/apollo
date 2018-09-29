organization_module.controller('OrganizationController',
                      ['$scope', '$window', 'toastr', 'AppService', 'AppUtil', 'OrganizationService',
                          OrganizationController]);

function OrganizationController($scope, $window, toastr, AppService, AppUtil, OrganizationService) {

    $scope.organization = {};
    init();
    function init() {
        initOrganization();
    }
    function initOrganization() {
        OrganizationService.find_organizations().then(function (result) {
            var organizations = [];
            result.forEach(function (item) {
                var org = {};
                org.id = item.orgId;
                org.text = item.orgName + '(' + item.orgId + ')';
                org.name = item.orgName;
                organizations.push(org);
            });
            $('#organization').select2({
                placeholder: '请选择部门',
                width: '100%',
                data: organizations
            });
        }, function (result) {
            toastr.error(AppUtil.errorMsg(result), "load organizations error");
        });
    }

    $scope.createOrUpdateUser = function () {
        UserService.createOrUpdateUser($scope.organization).then(function (result) {
            toastr.success("创建部门成功");
        }, function (result) {
            AppUtil.showErrorMsg(result, "创建部门失败");
        })

    }
}
