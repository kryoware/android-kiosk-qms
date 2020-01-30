package com.example.kiosk;

import android.content.Context;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Toast;

import com.example.hoinprinterlib.HoinPrinter;
import com.example.hoinprinterlib.module.PrinterCallback;
import com.example.hoinprinterlib.module.PrinterEvent;

import org.json.JSONException;
import org.json.JSONObject;

import io.sentry.Sentry;

public class JSBridge {
    Context mContext;
    WebView _webView;

    JSBridge(Context c, WebView webView) {
        mContext = c;
        _webView = webView;
    }

    @JavascriptInterface
    public void printTicket(String jsonStr) {
        try {
            JSONObject ticket = new JSONObject(jsonStr);

            String date, time, company_name, ticket_no, serving;
            Integer customers;

            date = ticket.getString("date");
            time = ticket.getString("time");
            company_name = ticket.getString("company_name");
            ticket_no = ticket.getString("ticket_no");
            serving = ticket.has("serving") ? ticket.getJSONObject("serving").getString("ticket_label") : null;
            customers = ticket.getInt("customers");

            try {
                HoinPrinter mHoinPrinter = HoinPrinter.getInstance(mContext, 2, new PrinterCallback() {
                    @Override
                    public void onState(int i) {
                        String message = "";

                        if (i == 6) {
                            message = "Printer Connected";
                        }

                        if (i == 7) {
                            message = "Printer Disconnected";
                        }

                        Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
                        Sentry.capture("PrinterCallback | onState: " + i);
                    }

                    @Override
                    public void onError(int i) {
                        String message = "";

                        if (i == 1006) {
                            message = "No USB Devices Found";
                        }

                        if (i == 1007) {
                            message = "No USB Permission";
                        }

                        Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
                        Sentry.capture("PrinterCallback | onError: " + i);
                    }

                    @Override
                    public void onEvent(PrinterEvent printerEvent) {
                        Sentry.capture("PrinterCallback | onEvent: " + printerEvent.toString());
                    }
                });

                mHoinPrinter.connect(null);
                mHoinPrinter.switchType(true);
                mHoinPrinter.printText(company_name, false, false, true, true);
                mHoinPrinter.printText("\n", false, false, false, true);
                mHoinPrinter.printText("Your Ticket Number", false, false, false, true);
                mHoinPrinter.printText("\n", false, false, false, true);
                mHoinPrinter.printText(ticket_no, true, true, true, true);
                mHoinPrinter.printText("\n", false, false, false, true);
                mHoinPrinter.printText("\n", false, false, false, true);
                mHoinPrinter.printText("Please be seated we will be attending to you shortly.", false, false, false, true);
                mHoinPrinter.printText("\n", false, false, false, true);

                if (serving != null) {
                    mHoinPrinter.printText("Latest Ticket Served: " + serving, false, false, false, true);
                    mHoinPrinter.printText("\n", false, false, false, true);
                }

                if (customers != 0) {
                    mHoinPrinter.printText("Total Customer(s) waiting: " + customers, false, false, false, true);
                    mHoinPrinter.printText("\n", false, false, false, true);
                }

                mHoinPrinter.printText("\n", false, false, false, true);
                mHoinPrinter.printText("\n", false, false, false, true);
                mHoinPrinter.printText(date + "\t\t\t\t" + time, false, false, false, true);
            } catch (Exception e) {
                Sentry.capture(e);
            }
        } catch (JSONException e) {
            Sentry.capture(e);
        }
    }
}