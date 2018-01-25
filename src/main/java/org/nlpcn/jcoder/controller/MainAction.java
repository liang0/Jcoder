package org.nlpcn.jcoder.controller;

import com.google.common.collect.ImmutableMap;

import com.alibaba.fastjson.JSONArray;

import org.apache.curator.framework.recipes.cache.ChildData;
import org.nlpcn.jcoder.domain.User;
import org.nlpcn.jcoder.filter.AuthoritiesManager;
import org.nlpcn.jcoder.service.SharedSpaceService;
import org.nlpcn.jcoder.service.TaskService;
import org.nlpcn.jcoder.util.Restful;
import org.nlpcn.jcoder.util.StaticValue;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.mvc.Mvcs;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.By;
import org.nutz.mvc.annotation.Filters;
import org.nutz.mvc.annotation.Ok;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by Ansj on 14/12/2017.
 */

@IocBean
@Filters(@By(type = AuthoritiesManager.class))
@Ok("json")
public class MainAction {

	private static final Logger LOG = LoggerFactory.getLogger(TaskAction.class);

	@Inject
	private TaskService taskService;


	@At("/admin/main/left")
	public Restful left() throws Exception {

		JSONArray result = new JSONArray();

		User user = (User) Mvcs.getHttpSession().getAttribute("user");

		boolean isAdmin = user.getType() == 1;

		StaticValue.space().getHostGroupCache().toMap();

		List<String> strings = StaticValue.space().getZk().getChildren().forPath("/jcoder/group");

		Set<String> allGroups = new TreeSet<>(strings);

		//task 管理
		JSONArray submenus = new JSONArray();
		for (String groupName : allGroups) {
			submenus.add(ImmutableMap.of("name", groupName.toString(), "url", "task/list.html?name=" + groupName));
		}
		result.add(ImmutableMap.of("name", "Task管理", "submenus", submenus));


		//Resource管理
		submenus = new JSONArray();
		for (String groupName : allGroups) {
			submenus.add(ImmutableMap.of("name", groupName.toString(), "url", "resource/list.html?name=" + groupName));
		}
		result.add(ImmutableMap.of("name", "Resource管理", "submenus", submenus));

		//Thread管理
		submenus = new JSONArray();
		for (String groupName : allGroups) {
			submenus.add(ImmutableMap.of("name", groupName.toString(), "url", "thread/index.html?name=" + groupName));
		}
		result.add(ImmutableMap.of("name", "Thread管理", "submenus", submenus));


		//系统管理
		submenus = new JSONArray();
		submenus.add(ImmutableMap.of("name", "用户管理", "url", "user/list.html"));
		submenus.add(ImmutableMap.of("name", "Group管理", "url", "group/list.html"));
		result.add(ImmutableMap.of("name", "系统管理", "submenus", submenus));


		//系统健康
		submenus = new JSONArray();

		Set<String> tempGroups = new HashSet<>(allGroups) ;


		//冲突的group
		List<String> groups = new ArrayList<>();
		StaticValue.space().getHostGroupCache().entrySet().forEach(e -> {
			int index = e.getKey().indexOf("_");
			String groupName = e.getKey().substring(index+1);
			if (!e.getValue().isCurrent()) {
				groups.add(groupName);

			}
			tempGroups.remove(groupName) ;
		});
		for (String group : groups) {
			submenus.add(ImmutableMap.of("name", "冲突：" + group, "url", "group/group_host_list.html?name=" + group));
		}

		//同步主机
		for (String group : tempGroups) {
			submenus.add(ImmutableMap.of("name", "无同步：" + group, "url", "group/list.html#"+group));
		}


		if (submenus.size() > 0) {
			result.add(ImmutableMap.of("name", "系统健康", "submenus", submenus));
		}


		return Restful.instance().obj(result);

	}
}