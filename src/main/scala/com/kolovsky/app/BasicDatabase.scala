package com.kolovsky.app
import java.sql.{Connection, DriverManager}

import com.kolovsky.graph.Edge
import com.kolovsky.modeler.Zone

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

/**
  * Created by kolovsky on 5.6.17.
  */
class BasicDatabase(uri: String, modelName: String) extends Database {
  override def getEdges(): Array[(Int, Int)] = {
    val connection = getConnection()
    connection.setAutoCommit(false)
    val sql = "SELECT source, target FROM "+modelName+".edge ORDER BY edge_id;"
    val st = connection.prepareStatement(sql)
    st.setFetchSize(10000)
    val rs = st.executeQuery()
    val edges: ArrayBuffer[(Int, Int)] = ArrayBuffer()
    while (rs.next()){
      edges += ((rs.getInt("source"), rs.getInt("target")))
    }
    edges.toArray
  }

  override def getCapacity(): Array[Double] = {
    val connection = getConnection()
    connection.setAutoCommit(false)
    val sql = "SELECT capacity FROM "+modelName+".edge ORDER BY edge_id;"
    val st = connection.prepareStatement(sql)
    st.setFetchSize(10000)
    val rs = st.executeQuery()
    val capacity: ArrayBuffer[Double] = ArrayBuffer()
    while (rs.next()){
      capacity += rs.getDouble("capacity")
    }
    capacity.toArray
  }

  override def getCost(): Array[Double] = {
    val connection = getConnection()
    connection.setAutoCommit(false)
    val sql = "SELECT cost FROM "+modelName+".edge ORDER BY edge_id;"
    val st = connection.prepareStatement(sql)
    st.setFetchSize(10000)
    val rs = st.executeQuery()
    val cost: ArrayBuffer[Double] = ArrayBuffer()
    while (rs.next()){
      cost += rs.getDouble("cost")
    }
    cost.toArray
  }

  def getId(): Array[Int] = {
    val connection = getConnection()
    connection.setAutoCommit(false)
    val sql = "SELECT edge_id FROM "+modelName+".edge ORDER BY edge_id;"
    val st = connection.prepareStatement(sql)
    st.setFetchSize(10000)
    val rs = st.executeQuery()
    val cost: ArrayBuffer[Int] = ArrayBuffer()
    while (rs.next()){
      cost += rs.getInt("edge_id")
    }
    cost.toArray
  }

  def getProfile(): Array[(Int, Double)]={
    val hm = mutable.HashMap.empty[Int, Int]
    val ids = getId()

    for(i <- ids.indices){
      hm += ((ids(i), i))
    }

    val connection = getConnection()
    connection.setAutoCommit(false)
    val sql = "SELECT edge_id, traffic FROM "+modelName+".profile;"
    val st = connection.prepareStatement(sql)
    st.setFetchSize(10000)
    val rs = st.executeQuery()
    val profiles: ArrayBuffer[(Int, Double)] = ArrayBuffer()
    while (rs.next()){
      val id =  rs.getInt("edge_id")
      val i = hm(id)
      profiles += ((i, rs.getDouble("traffic")))
    }
    profiles.toArray
  }

  override def getODM(): Array[(Zone, Zone, Double)] = {
    val connection = getConnection()
    connection.setAutoCommit(false)
    val sql = "SELECT source, source_node, target, target_node, flow FROM "+modelName+".odm;"
    val st = connection.prepareStatement(sql)
    st.setFetchSize(10000)
    val rs = st.executeQuery()
    val odm: ArrayBuffer[(Zone, Zone, Double)] = ArrayBuffer()
    while (rs.next()){
      val s = new Zone(rs.getInt("source"), rs.getInt("source_node"))
      val t = new Zone(rs.getInt("target"), rs.getInt("target_node"))
      odm += ((s, t, rs.getDouble("flow")))
    }
    odm.toArray
  }

  def getZones(): Array[(Zone, Double)] ={
    val connection = getConnection()
    connection.setAutoCommit(false)
    val sql = "SELECT zone_id, node_id, trips FROM "+modelName+".zone where trips != 0;"
    val st = connection.prepareStatement(sql)
    st.setFetchSize(10000)
    val rs = st.executeQuery()
    val edges: ArrayBuffer[(Zone, Double)] = ArrayBuffer()
    while (rs.next()){
      val z = new Zone(rs.getInt("zone_id"), rs.getInt("node_id"))
      edges += ((z, rs.getDouble("trips")))
    }
    edges.toArray
  }

  private def getConnection(): Connection ={
    try{
      Class.forName("org.postgresql.Driver")
      return  DriverManager.getConnection(uri)
    }catch {
      case e => e.printStackTrace
    }
    null
  }
}
