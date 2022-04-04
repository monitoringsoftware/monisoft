/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
package de.jmonitoring.Cluster;

import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;

/**
 *
 * @author togro
 */
public class Cluster {

    private String name;
    private HashMap<Integer, TreeSet<Integer>> clusterGroups = new HashMap<Integer, TreeSet<Integer>>();

    public Cluster(String name) {
        this.name = name;
    }

    public Cluster(String name, HashMap<Integer, TreeSet<Integer>> clusterGroups) {
        this.name = name;
        this.clusterGroups = clusterGroups;
    }

    public HashMap<Integer, TreeSet<Integer>> getClusterGroups() {
        return clusterGroups;
    }

    public void setClusterGroups(HashMap<Integer, TreeSet<Integer>> clusterGroups) {
        this.clusterGroups = clusterGroups;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addBuilding(Integer group, Integer building) {
        clusterGroups.get(group).add(building);
    }

    public void addBuildings(Integer group, TreeSet<Integer> buildings) {
        clusterGroups.put(group, buildings);
    }

    public void removeBuildingFromClusterGroup(Integer group, Integer building) {
        clusterGroups.get(group).remove(building);
    }

    public void removeBuilding(Integer building) {
        HashMap<Integer, TreeSet<Integer>> groups = getClusterGroups();
        Iterator<Integer> it = groups.keySet().iterator();
        Integer group;
        while (it.hasNext()) {
            group = it.next();
            removeBuildingFromClusterGroup(group, building);
        }
    }

    public Integer getGroupCount() {
        return clusterGroups.size();
    }

    public TreeSet<Integer> getBuildingsForGroup(Integer group) {
        if (clusterGroups.get(group) == null) {
            return new TreeSet<Integer>();
        }
        return clusterGroups.get(group);
    }

    public void addGroup(Integer group) {
        Integer newGroup = clusterGroups.size() + 1;
        TreeSet<Integer> emptyBuildingList = new TreeSet<Integer>();
        clusterGroups.put(newGroup, emptyBuildingList);
    }

    public void setGroup(Integer group) {
        TreeSet<Integer> emptyBuildingList = new TreeSet<Integer>();
        clusterGroups.put(group, emptyBuildingList);
    }

    public void removeGroup(Integer group) {
        clusterGroups.remove(group);
    }

    public void clear() {
        clusterGroups = new HashMap<Integer, TreeSet<Integer>>();
    }

    public boolean containsGroup(Integer probeGroup) {
        for (Integer group : clusterGroups.keySet()) {
            if (group.equals(probeGroup)) {
                return true;
            }
        }
        return false;
    }
}
