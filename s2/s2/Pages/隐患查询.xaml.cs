﻿using System;
using System.Collections.Generic;
using System.Json;
using System.Linq;
using System.Net;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Documents;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Animation;
using System.Windows.Shapes;
using Com.Aote.ObjectTools;
using Com.Aote.Controls;

namespace s2.Pages
{
    public partial class 隐患查询 : UserControl
    {
        public 隐患查询()
        {
            InitializeComponent();
        }

        private void btnSearch_Click(object sender, RoutedEventArgs e)
        {
          	SearchObject conditions = (criteriaPanel.DataContext as SearchObject);
            conditions.Search();
			WebClientInfo wci = App.Current.Resources["dbclient"] as WebClientInfo;
            String dt = " 1=1";
            if (StartDate.Text.Trim().Length != 0)
                dt = " DEPARTURE_TIME>='" + StartDate.Text + "'";
            if (EndDate.Text.Trim().Length != 0)
            {
                if (dt.Length > 3)
                    dt += " and DEPARTURE_TIME<='" + EndDate.Text + " 23:59:59'";
                else
                    dt = " DEPARTURE_TIME<='" + EndDate.Text + " 23:59:59' ";
            }
            dt = " and " + dt;
            checkerList.Path = "sql";
            checkerList.Names = "precaution,road,unit_name,cus_dom,cus_dy,cus_floor,cus_room,user_name,telphone,departure_time,precaution_notified";
            String sql = @"SELECT CAST (CASE f_heater_trapped WHEN 1 THEN '热水器直排或烟道未排出室外' ELSE	'' END AS VARCHAR (50)) + CAST (CASE f_furnace_trapped WHEN 1 THEN '壁挂锅炉直排或烟道未排出室外' ELSE '' END AS VARCHAR (50)) + CAST (	CASE f_precaution_kitchen WHEN 1 THEN '开放式或密闭式厨房' ELSE	'' END AS VARCHAR (50)) + CAST (CASE f_precaution_multisource WHEN 1 THEN '多种火源' ELSE '' END AS VARCHAR (50)) + CAST (CASE f_precaution_otheruse WHEN '卧室' THEN '更改用途(卧室)' WHEN '客厅' THEN	'更改用途(客厅)' WHEN '浴室' THEN '更改用途(浴室)' ELSE	'' END AS VARCHAR (50)) precaution,	road,unit_name,	cus_dom,cus_dy,cus_floor,cus_room,f_consumername user_name,f_consumerphone telphone,SUBSTRING (DEPARTURE_TIME, 1, 10) departure_time,CAST (	CASE ISNULL(f_renow_id, '')	WHEN '' THEN '否' ELSE '是'	END AS VARCHAR (50)) precaution_notified FROM T_INSPECTION WHERE 1=1 and {0}";
            checkerList.HQL = String.Format(sql, new string[]{conditions.Condition + dt}).Replace("\t", " ").Replace("\n", " ").Replace("\r", " ");
            checkerList.Load();
        }

          
    }

}
