package com.abc.bean;

import com.xtr.framework.hutool.IData;
import com.xtr.framework.hutool.IDataset;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 树形结构构建工具类
 */
public class TreeBuilder {

    /**
     * 构建菜单树形结构
     * @param menuList 扁平化的菜单列表
     * @return 树形结构数据
     */
    public static List<IData> buildTreeStructure(IDataset menuList,String Idata_id,String Idata_parent_id) {
        IData result = new IData();
        List<IData> allMenus = menuList.toList();

        // 创建一个映射，用于快速查找菜单项
        Map<Long, IData> menuMap = new HashMap<>();
        for (IData menu : allMenus) {
            Long id = menu.getLong(Idata_id);
            if (id != null) {
                menuMap.put(id, menu);
            }
        }

        // 创建一个映射，用于存储每个父菜单下的子菜单
        Map<Long, List<IData>> childrenMap = new HashMap<>();

        // 初始化childrenMap
        for (IData menu : allMenus) {
            Long parentId = menu.getLong(Idata_parent_id);
            // 如果parentId为null，将其视为0（根节点）
            if (parentId == null) {
                parentId = 0L;
            }
            childrenMap.computeIfAbsent(parentId, k -> new ArrayList<>()).add(menu);
        }

        // 查找根节点（parentId为0的菜单）
        List<IData> rootMenus = childrenMap.getOrDefault(0L, new ArrayList<>());

        // 为每个根节点构建其子树
        List<IData> tree = new ArrayList<>();
        for (IData rootMenu : rootMenus) {
            tree.add(buildSubtree(rootMenu, childrenMap));
        }

        // result.set("tree", tree);
        // result.set("count", menuList.size());
        return tree;
    }

    /**
     * 递归构建子树
     * @param menu 当前菜单节点
     * @param childrenMap 子菜单映射
     * @return 构建好的子树节点
     */
    private static IData buildSubtree(IData menu, Map<Long, List<IData>> childrenMap) {
        IData node = new IData();
        // 复制菜单的所有属性到节点
        for (String key : menu.keySet()) {
            node.set(key, menu.get(key));
        }

        // 获取当前菜单的子菜单
        List<IData> children = childrenMap.getOrDefault(menu.getLong("id"), new ArrayList<>());

        if (!children.isEmpty()) {
            List<IData> childNodes = new ArrayList<>();
            for (IData child : children) {
                childNodes.add(buildSubtree(child, childrenMap));
            }
            node.set("children", childNodes);
        } else {
            //叶子节点没有children属性
            //node.set("children", new ArrayList<>());
        }

        return node;
    }
}