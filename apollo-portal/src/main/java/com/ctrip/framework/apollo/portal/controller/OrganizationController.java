package com.ctrip.framework.apollo.portal.controller;


import com.ctrip.framework.apollo.common.utils.RequestPrecondition;
import com.ctrip.framework.apollo.portal.component.config.PortalConfig;
import com.ctrip.framework.apollo.portal.entity.po.ServerConfig;
import com.ctrip.framework.apollo.portal.entity.vo.Organization;
import com.ctrip.framework.apollo.portal.repository.ServerConfigRepository;
import com.ctrip.framework.apollo.portal.spi.UserInfoHolder;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static com.ctrip.framework.apollo.common.utils.RequestPrecondition.checkModel;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
@Api
@RestController
@RequestMapping("/organizations")
public class OrganizationController {
    @Autowired
    private ServerConfigRepository serverConfigRepository;
    @Autowired
    private UserInfoHolder userInfoHolder;
    @Autowired
    private PortalConfig portalConfig;
    private final Gson gson = new Gson();

    @RequestMapping(method = RequestMethod.GET)
    @ApiOperation(value = "查询部门列表")
    public List<Organization> loadOrganization() {
        return portalConfig.organizations();
    }

    @ApiOperation(value = "添加部门列表")
    @PreAuthorize(value = "@permissionValidator.isSuperAdmin()")
    @RequestMapping(method = RequestMethod.POST)
    public Organization addOrganization(@RequestBody Organization organization) {
        checkModel(Objects.nonNull(organization));
        RequestPrecondition.checkArgumentsNotEmpty(organization.getOrgId(), organization.getOrgName());
        String modifiedBy = userInfoHolder.getUser().getUserId();
        ServerConfig storedConfig = serverConfigRepository.findByKey("organizations");
        Date curr = new Date();
        if (Objects.isNull(storedConfig)) {
            ServerConfig serverConfig = new ServerConfig();
            List<Organization> values = new ArrayList<>(1);
            values.add(organization);
            serverConfig.setValue(gson.toJson(values));
            serverConfig.setKey("organizations");
            serverConfig.setComment("部门列表");
            serverConfig.setDeleted(false);
            serverConfig.setDataChangeCreatedTime(curr);
            serverConfig.setDataChangeLastModifiedTime(curr);
            serverConfig.setDataChangeCreatedBy(modifiedBy);
            serverConfig.setDataChangeLastModifiedBy(modifiedBy);
            serverConfigRepository.save(serverConfig);
        } else {
            if (Objects.nonNull(storedConfig.getValue())) {
                final List<Organization> organizationList = gson.fromJson(storedConfig.getValue(), new TypeToken<List<Organization>>() {
                }.getType());
                if (Objects.nonNull(organizationList) && !organizationList.contains(organization)) {
                    organizationList.add(organization);
                    storedConfig.setValue(gson.toJson(organizationList));
                    storedConfig.setDataChangeLastModifiedBy(modifiedBy);
                    serverConfigRepository.save(storedConfig);
                }
            }
        }
        return organization;
    }
}
