package com.example.aishpr.mooooood;
        import android.app.Activity;
        import android.graphics.Color;
        import android.graphics.DashPathEffect;
        import android.graphics.LinearGradient;
        import android.graphics.Paint;
        import android.graphics.Shader;
        import android.os.Bundle;
        import android.util.Log;

        import com.androidplot.Plot;
        import com.androidplot.util.PixelUtils;
        import com.androidplot.xy.SimpleXYSeries;
        import com.androidplot.xy.XYSeries;
        import com.androidplot.xy.*;
        import com.parse.FindCallback;
        import com.parse.ParseException;
        import com.parse.ParseObject;
        import com.parse.ParseQuery;
        import com.parse.ParseUser;

        import java.text.DecimalFormat;
        import java.util.ArrayList;
        import java.util.Arrays;
        import java.util.GregorianCalendar;
        import java.util.List;
        import java.util.Objects;
        import java.util.Observable;
        import java.util.Observer;
        import java.util.Calendar;


public class MoodSwingGraph extends Activity {
    /*
    private class MyPlotUpdater implements Observer {
        Plot plot;

        public MyPlotUpdater(Plot plot) {
            this.plot = plot;
        }

        @Override
        public void update(Observable o, Object arg) {
            plot.redraw();
        }
    }
    */
    private XYPlot MoodSwingsPlot;
    //private MyPlotUpdater plotUpdater;
    int totalFeelz;
    int happyFeelz;
    int currentTimeInEpoch;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.graphs);
        Calendar calendar = new GregorianCalendar();

        int minute = calendar.get(Calendar.MINUTE);
        /*if (minute == 0) {
            plotUpdater = new MyPlotUpdater(MoodSwingsPlot);
        }
        */
        currentTimeInEpoch = (int) System.currentTimeMillis() / 1000;
        totalFeelz = 0;
        happyFeelz = 0;

        final Float[] happyFeelzPercentages = new Float[24];

        for (int i = 0; i < 24; i++) {
            ParseQuery<ParseObject> query = ParseQuery.getQuery("MoodMessage");
            ParseUser user = ParseUser.getCurrentUser();
            query.whereLessThan("timestamp", currentTimeInEpoch);
//            query.whereGreaterThan("timestamp", (currentTimeInEpoch -3600));
            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> list, ParseException e) {
                    List <String> objectIds = new ArrayList<>();
                    for (ParseObject object : list) {
                        objectIds.add(object.getObjectId());
                    }

                    ParseQuery<ParseObject> query = ParseQuery.getQuery("MoodMessage");
                    query.whereGreaterThan("timestamp", (currentTimeInEpoch - 3600));
                    query.whereContainedIn("objectId", objectIds);

                    query.findInBackground(new FindCallback<ParseObject>() {
                        @Override
                        public void done(List<ParseObject> messageList, ParseException e) {
                            if (e == null) {
                                Log.d("timestamps", "retrieved " + messageList.size() + " messages");
                                for (ParseObject parseObject : messageList) {
                                    Log.d("task","Something" + parseObject.get("feelz").toString());
                                    String task;
                                    task = parseObject.get("feelz").toString();
                                    if (task.equals("happy")) {
                                        happyFeelz += 1;
                                    }
                                    totalFeelz++;

                                }
                            } else {
                                Log.d("timestamps", "Error: " + e.getMessage());
                            }
                        }
                    });
                }
            });
            float totalHappyFeelPercent;
            if (totalFeelz != 0) {
                totalHappyFeelPercent = (happyFeelz / totalFeelz) * 100;
            } else {
                totalHappyFeelPercent = 100;
            }
            happyFeelzPercentages[i] = totalHappyFeelPercent;
            currentTimeInEpoch =- 3600;
        }

        Integer[] timeArray = new Integer[24];
        for (int i = 0; i < 24; i++) {
            timeArray[i] = i;
        }
        MoodSwingsPlot = (XYPlot) findViewById(R.id.MoodSwingsPlot);
        MoodSwingsPlot.getGraphWidget().setDomainValueFormat(new DecimalFormat("0"));

        XYSeries series1 = new SimpleXYSeries(Arrays.asList(timeArray), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "Series1");

        XYSeries series2 = new SimpleXYSeries(Arrays.asList(happyFeelzPercentages), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "Series2");

        LineAndPointFormatter series1Format = new LineAndPointFormatter(
                Color.rgb(0, 200, 0),                   // line color
                Color.rgb(0, 100, 0),                   // point color
                null,                                   // fill color (none)
                new PointLabelFormatter(Color.WHITE));                           // text color

        MoodSwingsPlot.addSeries(series1, series1Format);

        // same as above:
        MoodSwingsPlot.addSeries(series2,
                new LineAndPointFormatter(
                        Color.rgb(0, 0, 200),
                        Color.rgb(0, 0, 100),
                        null,
                        new PointLabelFormatter(Color.WHITE)));

        MoodSwingsPlot.getGraphWidget().getGridBackgroundPaint().setColor(Color.WHITE);
        MoodSwingsPlot.getGraphWidget().getRangeGridLinePaint().setPathEffect(new DashPathEffect(new float[]{1, 1}, 1));
        MoodSwingsPlot.getGraphWidget().getDomainOriginLinePaint().setColor(Color.BLACK);
        MoodSwingsPlot.getGraphWidget().getRangeOriginLinePaint().setColor(Color.BLACK);

        MoodSwingsPlot.setBorderStyle(Plot.BorderStyle.SQUARE, null, null);
        MoodSwingsPlot.getBorderPaint().setStrokeWidth(1);
        MoodSwingsPlot.getBorderPaint().setAntiAlias(false);
        MoodSwingsPlot.getBorderPaint().setColor(Color.WHITE);

        // setup our line fill paint to be a slightly transparent gradient:
        Paint lineFill = new Paint();
        lineFill.setAlpha(200);
        lineFill.setShader(new LinearGradient(0, 0, 0, 250, Color.WHITE, Color.GREEN, Shader.TileMode.MIRROR));

        LineAndPointFormatter formatter = new LineAndPointFormatter(Color.rgb(0, 0, 0), Color.BLUE, Color.YELLOW, new PointLabelFormatter(Color.RED));
        formatter.setFillPaint(lineFill);
        MoodSwingsPlot.getGraphWidget().setPaddingRight(2);

        MoodSwingsPlot.setDomainStep(XYStepMode.INCREMENT_BY_PIXELS, 1);


    }
}