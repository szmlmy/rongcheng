using System;
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

namespace s2.Pages
{
    public partial class 多次未安检用户排查明细 : UserControl
    {
        public 多次未安检用户排查明细()
        {
            InitializeComponent();
        }

        private void btnSearch_Click(object sender, RoutedEventArgs e)
        {
            String dt = "1=1";
            if (StartDate.Text.Trim().Length != 0)
                dt = " DEPARTURE_TIME>='" + StartDate.Text + "'";   
            String dt2 = "1=1";
            if (StartDate.Text.Trim().Length != 0)
                dt2 = " DEPARTURE_TIME<='" + EndDate.Text + "'";
           
            checkerList.Path = "sql";
            checkerList.Names = "f_userid,f_username,cishu,f_districtname,f_cusDom,f_cusDy,f_cusFloor,f_apartment,f_phone,f_address";
            String sql = @"select e.f_userid,f_username,cishu,f_districtname,f_cusDom,f_cusDy,f_cusFloor,f_apartment,f_phone,f_address from t_userfiles u inner join
(select f_userid,COUNT(f_userid) as cishu from T_INSPECTION where " + dt + " and " + dt2 + "and (condition='拒检' or condition='无人') group by f_userid) e on u.f_userid=e.f_userid ";
            checkerList.HQL = sql;
            checkerList.Load();
        }
    }
}

