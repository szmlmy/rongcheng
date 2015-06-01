package com.aote.rs.bank.bankreturn;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateTemplate;

import com.aote.rs.BankService;


/**
 * 荣成返盘文件处理实现
 * @author Administrator
 *
 */
public class RongChengBankReturn implements BankReturnInterface {

	static Logger log = Logger.getLogger(BankService.class);
	
	@Autowired
	private HibernateTemplate hibernateTemplate;
	
	 
	@Override
	public void process(JSONArray datas) {
	
		
	}

}
