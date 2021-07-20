package cl.tofcompany.sift.Controllers.Drivers;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Pie;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;

import cl.tofcompany.sift.R;
import cl.tofcompany.sift.includes.MyToolbar;

public class TasadeAceptacion extends AppCompatActivity {
    //variable de api Char
    private AnyChartView anyChartView;
    //variable de api Char
    private LineChart lineChart;
    //variable de api Char
    private LineDataSet lineDataSet;
    //variable para los meses
 String [] months = {"jan","feb", "Mar"};
 int[] earnings = {1,1,1};
 String [] days = {"lunes","martes", "miecoles","jueves","viernes"};
 float numeroviajediaria = 12;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasade_aceptacion);
        //llamado mi toolbar
        MyToolbar.show(this,"",true);
        //iniciamos el anychar con su id
        anyChartView = findViewById(R.id.any_chart_view);
        //mostrar el grafico
        setupPieChart();
        //iniciamos el anychar con su id
        lineChart = findViewById(R.id.lineChart);
        //mostrar el grafico
        setupPieChart2();

    }
    //metodo de grafico pie
    void setupPieChart(){
        Pie pie = AnyChart.pie();
        //crear un lista
        List<DataEntry> dataEntries = new ArrayList<>();
        //recorrer
        for (int i = 0; i < months.length; i++){
            dataEntries.add(new ValueDataEntry(months[i], earnings[i]));
        }
        pie.data(dataEntries);
        anyChartView.setChart(pie);
    }
     void setupPieChart2() {
         ArrayList<Entry> lineEntries = new ArrayList<>();
         for (int i = 0; i<days.length; i++){
             float y = (int) (Math.random() * 8) + 1;
             lineEntries.add(new Entry((float) i,(float)y));
         }
         lineDataSet = new LineDataSet(lineEntries, "SIFT");
         LineData lineData = new LineData();
         lineData.addDataSet(lineDataSet);
         lineChart.setData(lineData);
    }


}