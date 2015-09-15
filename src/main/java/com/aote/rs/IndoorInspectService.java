package com.aote.rs;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.transform.Transformers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Component;

import com.aote.rs.charge.HandCharge;
import com.aote.rs.util.CalculateStairsPrice;


@Path("iis")
@Component
public class IndoorInspectService {
	static Logger log = Logger.getLogger(IndoorInspectService.class);
	boolean flag = false;
	@Autowired
	private HibernateTemplate hibernateTemplate;
	
	@Path("update")
	@POST
	@Produces("application/json")
	public String updateOrSave(String stringifiedObj) {
		log.debug("����İ����¼��" + stringifiedObj);
		try {
			JSONObject row = new JSONObject(stringifiedObj);
			//uuid���򣺵�½�û�id_����ƻ���ID
			String uuid = row.getString("ID");
			String planId = row.getString("CHECKPLAN_ID");
			String state = row.getString("CONDITION");
			String userid = row.getString("f_userid");

			DeletePossiblePriorRow(uuid, state);
			if(InsertNewRow(row))
				return "{\"ok\":\"ok\"}";
			else
				return "{\"ok\":\"nok\"}";				
		} catch (JSONException e) {
			return "{\"ok\":\"nok\"}";
		}
	}


	@Path("saveRepair")
	@POST
	@Produces("application/json")
	public String saveRepairResult(String stringifiedObj) {
		log.debug("�����ά�޽����" + stringifiedObj);
		try {
			//{ID:111111, 'option1':'option1', 'option2':'option2'}
			JSONObject row = new JSONObject(stringifiedObj);
			String uuid = row.getString("ID");
			//ɾ�����п��ܵ�ά�޽��
			String hql = "delete from T_REPAIR_RESULT where INSPECTION_ID ='" + uuid +"'";
			this.hibernateTemplate.bulkUpdate(hql);
			Iterator<String> keys = row.keys();
			while(keys.hasNext())
			{
				String value = keys.next();
				if(value.equals("ID"))
					continue;
				String sql = "INSERT INTO T_REPAIR_RESULT(ID, INSPECTION_ID, CONTENT)values('" 
					+ UUID.randomUUID().toString().replace("-", "") + "', '" + uuid + "', '"
					+ value + "')";
				execSQL(sql);
			}
			String dt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
			//���°���״̬
			String sql = "update T_INSPECTION set REPAIR_STATE='��ά��', REPAIR_DATE='" + dt + "' where id='" + uuid +"'";
			execSQL(sql);
			return "{\"ok\":\"ok\"}";				
		} catch (Exception e) {
			return "{\"ok\":\"nok\"}";
		}
	}
	
	private void DeletePossiblePriorRow(String uuid, String state) {
		String hql = "delete from T_INSPECTION_LINE where INSPECTION_ID ='" + uuid + "_1' or INSPECTION_ID ='" + uuid + "'";
		this.hibernateTemplate.bulkUpdate(hql);
		hql = "delete from T_INSPECTION where id ='" + uuid + "_1' or id ='" + uuid + "'";
		this.hibernateTemplate.bulkUpdate(hql);
	}


	@Path("SearchSCInfo/{f_userid}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String SearchInfo(@PathParam("f_userid") String f_userid) {
		log.debug("��ѯ�ϴΰ����¼");
		try {
			final String sql = "select f_jujian,f_sibiao,f_changtong,f_fanzhuang,f_qblouqi,f_reading_mismatch,f_meter_wrapped,f_meter_hanger"+
",f_meter_nearfire,f_meter_xiushi,f_lgsigai,f_lglouqi,f_lgbaoguo,f_lgguawu,f_lghuoyuan,f_lgweiguding,f_lgchuanyue,f_lgfushi"+
",f_plumbing_leakage_valve,f_plumbing_leakage_scaleknot,f_plumbing_leakage_slipknot,f_bhgbaoguan,f_bhgguawu,f_bhglouqi,f_bhgjinzhiquyu"+
",f_bhgdianyuan,f_bhggaiguan,f_bhgfushi,f_bhgwxd,f_heat_radiation,f_jpglouqi,f_jpglaohua,f_jpgguochang,f_jpgwuguanjia,f_jpgdiaoding"+
",f_cooker_overdue,f_cooker_nofireprotection,f_heater_overdue,f_heater_softconnector,f_heater_trapped,f_heater_leakage,f_bothyandao"+
",f_furnace_overdue,f_furnace_softconnector,f_furnace_trapped,f_furnace_leakage,f_precaution_kitchen,f_precaution_multisource"+
",f_heater_leakage_connetor,f_heater_leakage_valve,f_heater_leakage_heater"+
",f_furnace_leakage_connetor,f_furnace_leakage_valve,f_furnace_leakage_furnace,f_rgcq,f_rgyjk,f_dxssfylg,"+
"f_consumername,f_renkou,f_kahao,f_biaohao,f_consumerphone,f_alarm_installation_time,f_alarm_expire_time,f_zuzhu,f_property,f_iszhongdian,f_gongnuan,f_baojingqi,f_baojingqichang"+
",f_meter_type,f_rqbiaoxing,f_biaochang,f_kachangjia,f_meter_manufacture_date,f_meter_manufacfalse_date,f_installationlocation,f_TIE from T_INSPECTION t where t.f_userid = '" + f_userid + "' order by ARRIVAL_TIME desc";
			List list = (List) hibernateTemplate
				.execute(new HibernateCallback() {	
					@Override
					public Object doInHibernate(Session session) 
						throws HibernateException,
							SQLException {
						SQLQuery query = session.createSQLQuery(sql);
						return query.list();
					}
				});
			if(list.size() == 0)
				return "{\"ok\":\"nook\"}";
			Object obj = list.get(0);
			JSONObject json = new JSONObject();
			for (Object obj1 : list) {
				// �ѵ���mapת����JSON����
				Object[] c = (Object[]) obj1;
				for (int i = 0; i < c.length; i++) {
					try {
						json.put("col" + i, c[i]);
					} catch (JSONException e) {
						throw new WebApplicationException(400);
					}
				}
				break;
			}
			return json.toString();			
		} catch (Exception e) {
			return "{\"ok\":\"nok\"}";
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

	/**
	 * �����뻧��������
	 * @param row
	 * @throws JSONException 
	 */
	private boolean InsertNewRow(JSONObject row) throws JSONException {
			String uuid = row.getString("ID");
			String planId = row.getString("CHECKPLAN_ID");
			boolean isEntryInspection = row.getString("CONDITION").equals("����");
			
			//�ӱ��д��������
			Map<String, String> redundantCols = new HashMap<String, String>();
			if(isEntryInspection)
			{
				redundantCols.put("CARD_ID", row.getString("CARD_ID"));
				//redundantCols.put("CARD_ID", row.getString("CARD_ID"));
				redundantCols.put("USER_NAME", row.getString("USER_NAME"));
				redundantCols.put("ROAD", row.getString("ROAD"));
				redundantCols.put("UNIT_NAME", row.getString("UNIT_NAME"));
				redundantCols.put("CUS_DOM", row.getString("CUS_DOM"));
				redundantCols.put("CUS_DY", row.getString("CUS_DY"));
				redundantCols.put("CUS_FLOOR", row.getString("CUS_FLOOR"));
				redundantCols.put("CUS_ROOM", row.getString("CUS_ROOM"));
				redundantCols.put("TELPHONE", row.getString("TELPHONE"));
				redundantCols.put("SAVE_PEOPLE", row.getString("SAVE_PEOPLE"));
				redundantCols.put("SAVE_DATE", row.getString("DEPARTURE_TIME"));
				redundantCols.put("IC_METER_NAME", row.getString("IC_METER_NAME"));
				redundantCols.put("JB_METER_NAME", row.getString("JB_METER_NAME"));
				redundantCols.put("JB_NUMBER", row.getString("JB_NUMBER"));
				redundantCols.put("SURPLUS_GAS", row.getString("SURPLUS_GAS"));
			}
			
			String sql1 = "INSERT INTO T_INSPECTION(ID";
			String sql2 = ") VALUES('" + uuid +"'";
			//�������¼
			Map<String, String> masterMap = new HashMap<String, String>();
			Map<String, String> slaveMap = new HashMap<String, String>();
			Iterator<String> itr = row.keys();
			while(itr.hasNext())
			{
				String key = itr.next();
				if(key.equals("ID"))
					continue;
				if(key.matches(".*_\\d{1,2}$"))
					slaveMap.put(key, row.getString(key));
				else
				{
					sql1 += "," + key;
					if(key.equals("JB_NUMBER") || key.equals("SURPLUS_GAS"))
						sql2 += "," + (row.getString(key).length()>0 ? row.getString(key) :  "NULL");
					else
						sql2 += ",'" + row.getString(key).replace("'", "''") + "'";
				}
			}
			//����Ӽ�¼
			sql1 += sql2 +")";
			execSQL(sql1);
			if(isEntryInspection)
				InsertPrecaution(uuid, redundantCols, slaveMap);
			DeletePics(uuid);
			return true;
	}
	
	private void DeletePics(String uuid) {
		String hql = "from T_INSPECTION t where t.id =?";
		List lst = hibernateTemplate.find(hql, uuid);
		if(lst.size() == 0)
			return;
		Object obj = lst.get(0);
		Map<String, Object> map = (Map<String, Object>) obj;
		String photo = (String)map.get("USER_SIGN");		
		if(photo == null || photo.length()==0)
		{
			hql = "delete from T_INSPECTION where id ='" + uuid + "_sign'";
			this.hibernateTemplate.bulkUpdate(hql);
		}
		photo = (String)map.get("PHOTO_FIRST");		
		if(photo == null || photo.length()==0)
		{
			hql = "delete from T_INSPECTION where id ='" + uuid + "_1'";
			this.hibernateTemplate.bulkUpdate(hql);
		}
		photo = (String)map.get("PHOTO_SECOND");		
		if(photo == null || photo.length()==0)
		{
			hql = "delete from T_INSPECTION where id ='" + uuid + "_2'";
			this.hibernateTemplate.bulkUpdate(hql);
		}
		photo = (String)map.get("PHOTO_THIRD");		
		if(photo == null || photo.length()==0)
		{
			hql = "delete from T_INSPECTION where id ='" + uuid + "_3'";
			this.hibernateTemplate.bulkUpdate(hql);
		}
		photo = (String)map.get("PHOTO_FOUTH");		
		if(photo == null || photo.length()==0)
		{
			hql = "delete from T_INSPECTION where id ='" + uuid + "_4'";
			this.hibernateTemplate.bulkUpdate(hql);
		}
		photo = (String)map.get("PHOTO_FIFTH");	
		if(photo == null || photo.length()==0)
		{
			hql = "delete from T_INSPECTION where id ='" + uuid + "_5'";
			this.hibernateTemplate.bulkUpdate(hql);
		}
	}


	/**
	 * execute sql in hibernate
	 * @param sql
	 */
	private void execSQL(final String sql) {
        hibernateTemplate.execute(new HibernateCallback() {
            public Object doInHibernate(Session session)
                    throws HibernateException {
                session.createSQLQuery(sql).executeUpdate();
                return null;
            }
        });		
	}


	/**
	 * ������������
	 * @param uuid
	 * @param redundantCols
	 * @param slaveMap
	 * @param db
	 */
	private void InsertPrecaution(String uuid,
			Map<String, String> redundantCols, Map<String, String> slaveMap) {
		String snippet1 = "";
		String snippet2 = "";
		Map<String, String> map = new HashMap<String, String>();
		for (Map.Entry<String, String> entry : redundantCols.entrySet()) {
		    snippet1 += "," +  entry.getKey();
			if(entry.getKey().equals("JB_NUMBER") || entry.getKey().equals("SURPLUS_GAS"))
				snippet2 += "," + (entry.getValue().length()>0?entry.getValue():"NULL") + "";
			else
				snippet2 += ",'" + entry.getValue().replace("'", "''") + "'";
		}
		for (Map.Entry<String, String> entry : slaveMap.entrySet()) {
			int offset = 2;
			if(entry.getKey().charAt(entry.getKey().length()-3) == '_')
				offset = 3;
			String sql = "INSERT INTO T_INSPECTION_LINE(ID,INSPECTION_ID" + snippet1 + ",EQUIPMENT,CONTENT" 
			+  ") VALUES('" + UUID.randomUUID().toString().replace("-", "") + "','" + uuid +"'" + snippet2  +",'"
			+ entry.getKey().substring(0, entry.getKey().length()-offset) +"','"
			+ entry.getValue().replace("'", "''") + "')";
			execSQL(sql);
		}
	}
	
	@Path("CAupdate/{operator}/{department}")
	@POST
	@Produces("application/json")
	public String CAUpdateOrSave(@PathParam("operator") String operator,
								@PathParam("department") String department,
								String stringifiedObj) {
		log.debug("����İ����¼��" + stringifiedObj);
		try {
			JSONObject row = new JSONObject(stringifiedObj);
			//uuid���򣺵�½�û�id_����ƻ���ID
			String uuid = row.getString("ID");
			String planId = row.getString("CHECKPLAN_ID");
			String state = row.getString("CONDITION");
			String userid = row.getString("f_userid");
			final	String sq ="select * from T_INSPECTION where f_userid="+userid+" and CHECKPLAN_ID="+planId+"";
			List list1 = (List) hibernateTemplate
					.execute(new HibernateCallback() {
						public Object doInHibernate(Session session)
								throws HibernateException {
							SQLQuery query = session.createSQLQuery(sq);
							return query.list();
						}
					});
			if(list1.size()==1){
				flag = true;
			}
			CADeletePossiblePriorRow(uuid, state);
			if(CAInsertNewRow(row,operator,department, flag))
			{
				flag = false;
				return "{\"ok\":\"ok\"}";
			}
			else
			{
				flag = false;
				return "{\"ok\":\"nok\"}";
			}
		} catch (JSONException e) {
			flag = false;
			return "{\"ok\":\"nok\"}";
		}
	}
	
	/**
	 * ��������û�ʵʱ����
	 */
	@GET
	@Path("CalcCurrentBalance/{UserID}/{MeterNum}")
	@Produces(MediaType.APPLICATION_JSON)
	public String CalculateCurrentBalance(@PathParam("UserID") String UserID, @PathParam("MeterNum") String MeterNum/*���γ���ָ��*/)
	{
		log.debug("����ı��γ���ָ����" + MeterNum + ", �����UserID��" + UserID);
		String FindUserFileSql = "from t_userfiles where f_userid = '" + UserID + "'";
		List<Object> UserFileList = new ArrayList<Object>();
		Map<String, Object> UserFileMap = new HashMap<String, Object>();
		String GetArrearsMoneysql = "from t_handplan where f_userid = '" + UserID + "' and f_gasmeterstyle = '����' and shifoujiaofei='��' and f_state = '�ѳ���'";
		List<Object> GetArrearsMoneyList = new ArrayList<Object>();
		List<Map<String, Object>> GetArrearsMoneyMapList = new ArrayList<Map<String, Object>>();
		Map<String, Object> GetArrearsMoneyMap = new HashMap<String, Object>();
		String ZHYEStr = "";
		String LastRecordStr = "";
		String ArrearsMoneyStr = "";
		//�˻����
		double ZHYE = 0;
		//�ϴγ���ָ��
		double LastRecord = 0;
		//����������
		double GasNum = 0;
		//�����������
		double GasMoney = 0;
		//�ۼ�Ƿ�ѽ��
		double ArrearsMoney = 0;
		//��ǰ���� = ���� - �ۼ�Ƿ�� - �����������
		double CurrentBalance = 0;
		try {
			UserFileList = this.hibernateTemplate.find(FindUserFileSql);
			if(UserFileList.size() != 1)
			{
				//��ͨ��userid����û�������ֹһ�����򷵻ش���
				return "{\"ok\":\"err1\"}";
			}
			UserFileMap = (Map<String, Object>) UserFileList.get(0);
			ZHYEStr = UserFileMap.get("f_zhye") == null ? "0" : UserFileMap.get("f_zhye").toString();
			//�õ��˻����
			ZHYE = Double.parseDouble(ZHYEStr);
			LastRecordStr = UserFileMap.get("lastrecord") == null ? "0" : UserFileMap.get("lastrecord").toString();
			LastRecord = Double.parseDouble(LastRecordStr);
			if(Double.parseDouble(MeterNum) < LastRecord)
			{
				//�����γ���ָ��С���ϴγ���ָ�����򷵻ش���
				return "{\"ok\":\"err2\"}";
			}
			GasNum = Double.parseDouble(MeterNum) - LastRecord;
			//���㱾���������������������
			Map<String, Object> GasMoneyMap = new HashMap<String, Object>();
			GasMoneyMap = CalculateStairsPrice.CalculateGasMoney(GasNum, UserFileMap);
			String GasMoneyStr = GasMoneyMap.get("chargenum") == null ? "0" : GasMoneyMap.get("chargenum").toString();
			//�õ������������
			GasMoney = Double.parseDouble(GasMoneyStr);
			CurrentBalance = ZHYE - GasMoney;
			GetArrearsMoneyList = this.hibernateTemplate.find(GetArrearsMoneysql);
			if(GetArrearsMoneyList.size() == 0)
			{
				return "{\"ok\":\"" + CurrentBalance + "\"}";
			}
			for(int i = 0; i < GetArrearsMoneyList.size(); i++)
    		{
				GetArrearsMoneyMapList.add((Map<String, Object>) GetArrearsMoneyList.get(i));
    		}
			for(int j = 0; j < GetArrearsMoneyMapList.size(); j++)
			{
				ArrearsMoneyStr = GetArrearsMoneyMapList.get(j).get("oughtfee").toString();
				ArrearsMoney += Double.parseDouble(ArrearsMoneyStr);
			}
			CurrentBalance = ZHYE - ArrearsMoney - GasMoney;
			return "{\"ok\":\"" + CurrentBalance + "\"}";
		} catch (Exception e) {
			e.printStackTrace();
			//�����쳣���򷵻ش���
			return "{\"ok\":\"err4\"}";
		}
	}

	/**
	 * 
	 * @param road
	 * @param unit
	 * @param building
	 * @param dy
	 * @param floor
	 * @param room
	 * @param uuid
	 * @param dt
	 * @param reading
	 * @return
	 */
	@GET
	@Path("CAValidate/{newmeter}/{road}/{unit}/{building}/{dy}/{floor}/{room}/{uid}/{dt}/{reading}")
	@Produces(MediaType.APPLICATION_JSON)
	public String CAValidate(@PathParam("newmeter") String newmeter,
			@PathParam("road") String road, @PathParam("unit") String unit,
			@PathParam("building") String building, @PathParam("dy") String dy,
			@PathParam("floor") String floor, @PathParam("room") String room,
			@PathParam("uid") String uid, @PathParam("dt") String dt,
			@PathParam("reading") String reading) {
		log.debug("����Ĳ�����" + dt + "'" + reading);
		// �ڰ����¼�в�����һ�ΰ������ںͶ������������С���ϴΣ��ж��ڼ��Ƿ񻻱�
		// �������ڱ�������ϴ�
		try {
			final String sql = "select top 1 f_jbdushu, arrival_time from (select f_jbdushu, arrival_time from t_inspection "
					+ " where ROAD ='"
					+ road
					+ "' and UNIT_NAME='"
					+ unit
					+ "' and CUS_DOM='"
					+ building
					+ "' and CUS_DY='"
					+ dy
					+ "' and CUS_FLOOR='"
					+ floor
					+ "' and CUS_ROOM='"
					+ room
					+ "' and condition='����'"
					+ " ) t order by arrival_time desc";
			List list = (List) hibernateTemplate
					.execute(new HibernateCallback() {
						public Object doInHibernate(Session session)
								throws HibernateException {
							SQLQuery query = session.createSQLQuery(sql);
							return query.list();
						}
					});
			// �ҵ������¼���ж����ںͻ������
			if (list.size() == 1) {
				Object[] obj = (Object[]) list.get(0);
				String olddt = (String) obj[1];
				int oldreading = Integer.parseInt(obj[0].toString());
				// ������ڴ��ڵ�ǰ���죬���ش���
				if (olddt.compareTo(dt) > 0)
					return "{\"ok\":\"nok\", \"msg\":\"�Ѵ��ڵ�ǰ�û��ĳ�ǰ���졣�ϴΰ�������Ϊ"
							+ olddt + "\"}";
				// �����������ڵ�ǰ�������ж�ʱ������Ƿ񻻱�
				int newreading = Integer.parseInt(reading);
				if (oldreading > newreading  && !newmeter.equals("true")) {
					return "{\"ok\":\"nok\", \"msg\":\"�ϴγ���������ڱ��Ρ��ϴλ������Ϊ"	+ oldreading + "\"}";
				}
			}
			return "{\"ok\":\"ok\"}";
		} catch (Exception e) {
			return "{\"ok\":\"nok\"}";
		}
	}

	private void CADeletePossiblePriorRow(String uuid, String state) {
		String hql = "delete from T_INSPECTION_LINE where INSPECTION_ID ='" + uuid.replace("'", "") + "_1' or INSPECTION_ID =" + uuid;
		this.hibernateTemplate.bulkUpdate(hql);
		hql = "delete from T_INSPECTION where id ='" + uuid.replace("'", "") +"_1' or id =" + uuid;
		this.hibernateTemplate.bulkUpdate(hql);	
	}

	/**
	 * �����뻧��������
	 * @param row
	 * @throws JSONException 
	 */
	private boolean CAInsertNewRow(JSONObject row,String operator,String department, boolean flag) throws JSONException {
			String uuid = row.getString("ID");
			String condition = row.getString("CONDITION");
			String userid = row.getString("f_userid");
			String road = row.getString("ROAD");
			String unitName = row.getString("UNIT_NAME");
			String cusDom = row.getString("CUS_DOM");
			String cusDy = row.getString("CUS_DY");
			String cusFloor = row.getString("CUS_FLOOR");
			String cusRoom = row.getString("CUS_ROOM");
			String checkPlanID = row.getString("CHECKPLAN_ID");
			String checkDate = row.getString("ARRIVAL_TIME");
			
			int conditionFlag = 0;
			if(condition.equals("'����'"))
			{
				conditionFlag = 3;
			}
			else if(condition.equals("'�ܼ�'"))
			{
				conditionFlag = 33;
			}
			else if(condition.equals("'����'"))
			{
				conditionFlag = 65;
			}
			if(row.has("NEEDS_REPAIR"))
			{
				if(row.getString("NEEDS_REPAIR").equals("'��'"))
				{
					conditionFlag += 16;
				}
			}
			String suggestions = null; 
			String sql1 = "INSERT INTO T_INSPECTION(ID";
			String sql2 = ") VALUES(" + uuid;
			//�������¼
			Iterator<String> itr = row.keys();
			while(itr.hasNext())
			{
				String key = itr.next();
				if(key.equals("suggestions"))
				{
					suggestions = row.getString(key);
					continue;
				}
				if(key.equals("ID"))
					continue;
				sql1 += "," + key;
				sql2 += "," + row.getString(key);
			}
			//����Ӽ�¼
			sql1 += sql2 +")";
			execSQL(sql1);
			//����T_IC_SAFECHECK_PAPER�а�������û��İ���״̬
			if(road != null && unitName != null && cusDom != null && cusDy != null && cusFloor != null && cusRoom != null && checkPlanID != null)
			{
				String updateSql = "update T_IC_SAFECHECK_PAPER set CONDITION = '" + conditionFlag + "' where f_userid = " + userid;
				execSQL(updateSql);
			}
			sql1 = "update t_inspection set f_anjianriqi= substring(arrival_time,1,10) where id=" + uuid;
			execSQL(sql1);
			//�����û����������Ǽ��´ΰ�������
			String updateSQL = "update t_userfiles set f_toBeInspected = '��', " +
							"f_nextCheckDate = DATEADD(YEAR, 1, SUBSTRING(" + checkDate + ", 1, 10)) " +
							"where f_userid = " + userid;
			execSQL(updateSQL);
			StringBuffer sb = new StringBuffer("");
			if(suggestions != null)
			{
				JSONArray lines = new JSONArray(suggestions);
				for(int i=0; i<lines.length(); i++)
				{
					JSONObject line = lines.getJSONObject(i);
					sql1 = "insert into T_INSPECTION_LINE(id,EQUIPMENT,PARAM,VALUE,NAME,BZ,INSPECTION_ID) VALUES('" + UUID.randomUUID().toString() 
					+ "','" + line.getString("EQUIPMENT") 
					+ "','" + line.getString("PARAM") 
					+ "','" + line.getString("VALUE") 
					+ "','" + line.getString("NAME") 
					+ "','" + line.getString("BZ") 
					+ "','" + line.getString("INSPECTION_ID")  + "')";
					execSQL(sql1);
					sb.append(line.getString("EQUIPMENT")+":"+line.getString("NAME")+"\n");
				}
				
				String state = "'δ�ɷ�'";
				String accepter = null;
				String substate = null;
				String sender = null;
				String senddate = null;
				String sendtime = null;
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				Date d = new Date();
				String date = sdf.format(d);
				List list = (List) hibernateTemplate
						.execute(new HibernateCallback() {
							public Object doInHibernate(Session session)
									throws HibernateException {
								SQLQuery query = session.createSQLQuery("select value from t_singlevalue where name='����ģʽ'");
								return query.list();
							}
						});
				if(list.size()==1){
					String sta = list.get(0) + "";
					if("ֱ���ɷ���APPģʽ".equals(sta)){
						state = "'���ɷ�'";
						accepter = row.getString("REPAIRMAN");
						substate = "'δ����'";
						sender = "'"+operator+"'";
						senddate = "'"+date.substring(0,10)+"'";
						sendtime = "'"+date.substring(11,19)+"'";
					}
				}
				String uuid2 = UUID.randomUUID().toString();
				updateSQL = "insert into t_repairsys(id,anjianid,f_userid,f_username,f_linktype,"
						+ "f_address,f_metergasnums,f_operator,f_usertype,f_meetunit,"
						+ "f_repairreason,f_department,f_teltype,f_repairtype,"
						+ "f_dealtype,f_dealonline,f_havacomplete,f_havadeal,f_meternumber,"
						+ "f_gasmeteraccomodations,f_metertype,f_aroundmeter,f_recorddate,f_reporttime,"
						+ "f_cucode,f_state,f_accepter,f_substate,f_sender,"
						+ "f_senddate,f_sendtime)" +
						"VALUES('"+uuid2+"',"+uuid+","+userid+","+row.getString("f_consumername")+","+row.getString("f_consumerphone")+",'"+
						rejectionNull(road)+rejectionNull(unitName)+rejectionNull(cusDom)+rejectionNull(cusDy)+rejectionNull(cusFloor)+rejectionNull(cusRoom)+"',"+row.getString("f_buygas")+",'"+operator+"',(select f_usertype from t_userfiles where f_userid="+userid+"),"+row.getString("f_department")+",'"+
						sb.toString()+"','"+department+"',"+"'����תά��'"+","+"'����תά��'"+","+
						"'����תά��'"+","+"'����תά��'"+","+"'δ���'"+","+"'��'"+","+row.getString("f_biaohao")+","+
						row.getString("f_jbdushu")+","+row.getString("f_meter_type")+","+row.getString("f_rqbiaoxing")+",'"+date.substring(11,19)+"','"+date.substring(0,10)+"','aj"+
						new Date().getTime()+"',"+state+","+accepter+","+substate+","+sender+","+
						senddate+","+sendtime+")";
				execSQL(updateSQL);
			}
/*��ѯ���쵥�����ð��쵥�Ƿ����ϴ���
 * T_INSPECTION�������֤���У�ֱ��return true
 * 
 * */			
			

			if(true == flag)
				return true;
			
	/*�����ǰ��쳭�����
	 * �ӵ���������ȡ���û���һЩ������Ϣ
	 * ���磺�ϴγ��������û����ͣ����۵�*/
			final	String sql ="select lastinputgasnum,f_usertype,f_gasprice,f_gaspricetype,f_yhxz,f_weizhi from t_userfiles  where f_userid="+userid+"";
			List list0 = (List) hibernateTemplate
					.execute(new HibernateCallback() {
						public Object doInHibernate(Session session)
								throws HibernateException {
							SQLQuery query = session.createSQLQuery(sql);
							return query.list();
						}
					});
		
			if(list0.size()==1){
				Object [] o0=(Object[]) list0.get(0);
				double d1=Double.parseDouble(o0[0]+"");
				String f_usertype="'" + o0[1] + "'";
				String f_gasprice="'" + o0[2] + "'";
				String f_gaspricetype="'" + o0[3] + "'";
				String f_yhxz="'" + o0[4] + "'";
				String f_weizhi="'" + o0[5] + "'";
				String s2=o0[0]+"";
//�ж��ϴγ������Ƿ�Ϊ��				|| s2.equals("")
				if(s2!=null){
//	�����γ����������ϴγ�����ʱ�����ɳ���				
					if(Double.parseDouble(row.getString("f_jbdushu"))>d1){
						updateSQL = "insert into t_handplan(f_userid, f_username, lastinputgasnum, f_gaswatchbrand, f_metertype,"+
									"f_address, f_districtname, f_usertype, f_gasprice, f_gaspricetype,  f_apartment,"+
									"f_phone, f_inputtor, f_yhxz, f_weizhi,"+
									"f_state, shifoujiaofei, f_cusDom, f_cusDy)"+ 
								"VALUES("+userid+","+row.getString("f_consumername")+","+d1+","+row.getString("f_biaochang")+","
								+row.getString("f_meter_type")+","+"'"+row.getString("UNIT_NAME").replace("'", "")+row.getString("CUS_DOM").replace("'", "")+
								row.getString("CUS_DY").replace("'", "")+row.getString("CUS_FLOOR").replace("'", "")+row.getString("CUS_ROOM").replace("'", "")+"',"
								+row.getString("UNIT_NAME")+","+f_usertype+","+f_gasprice+","+f_gaspricetype+","+
								row.getString("CUS_ROOM")+","+row.getString("f_consumerphone")+","+row.getString("SAVE_PEOPLE")
								+","+f_yhxz+","+f_weizhi+","+"'δ����'"+","+"'��'"+","+row.getString("CUS_DOM")+","+row.getString("CUS_DY")+
								")";
						execSQL(updateSQL);
						

						Double jbds = Double.parseDouble(row.getString("f_jbdushu"));
						String wd=row.getString("SAVE_PEOPLE").replace("'", "");
						Date now = new Date();
						Calendar cal = Calendar.getInstance();
						DateFormat d23 = new SimpleDateFormat("yyyy-MM-dd");
						String cbrq = d23.format(now).replace("'", "");
						DateFormat d24 = new SimpleDateFormat("yyyy-MM");
						String month = d24.format(now).replace("'", "");
						
						String url = "http://127.0.0.1:8080/rs/handcharge/record/one/"+userid.replace("'", "")+"/"+jbds+"/"+wd+"/"+wd+"/"+cbrq+"/"+month+"/"+"����";
						DefaultHttpClient httpclient = new DefaultHttpClient();
						try {
							HttpGet getRequest = new HttpGet(url);
							HttpResponse httpResponse = httpclient.execute(getRequest);
							HttpEntity entity = httpResponse.getEntity();
							if (entity != null){
								String info = EntityUtils.toString(entity);
							   if("".equals(info))
								   return false;
						    }
							else
								return false;
							httpclient.getConnectionManager().shutdown();

						} catch (Exception e) {
							httpclient.getConnectionManager().shutdown();
							throw new WebApplicationException(400);
						} 
						
						
						
						
						
					}
					else 
						return false;
					
				}
				else
					return false;

			}
			

			DeletePics(uuid.replace("'", ""));
			return true;
	}
	private String rejectionNull(String s){
		if("'null'".equals(s.toLowerCase()))
			return "";
		return s.replace("'", "");
	}
}
