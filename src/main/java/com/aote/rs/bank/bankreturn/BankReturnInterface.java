package com.aote.rs.bank.bankreturn;

import org.codehaus.jettison.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateTemplate;

import com.aote.expression.Param;
import com.aote.expression.paramprocessor.NoFitValueException;


/**
 * ���з��̽ӿ�
 * @author Administrator
 *
 */
public interface BankReturnInterface {
	
	 /**
	  *������	
	  * @param param
	  * @return
	  */
	public void process(JSONArray datas)  ;
	

}
