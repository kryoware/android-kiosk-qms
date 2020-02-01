package com.example.kiosk;

import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import net.posprinter.posprinterface.ProcessData;
import net.posprinter.posprinterface.UiExecute;
import net.posprinter.utils.DataForSendToPrinterPos58;
import net.posprinter.utils.DataForSendToPrinterPos80;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.sentry.Sentry;

public class JSBridge {
    Context mContext;
    WebView _webView;

    JSBridge(Context c, WebView webView) {
        mContext = c;
        _webView = webView;
    }

    @JavascriptInterface
    public void printTicket(final String jsonStr) {
        try {
            JSONObject ticket = new JSONObject(jsonStr);

            final String date = ticket.getString("date");
            final String time = ticket.getString("time");
            final String company_name = ticket.getString("company_name");
            final String ticket_no = ticket.getString("ticket_no");
            final String serving = ticket.has("serving") ? ticket.getJSONObject("serving").getString("ticket_label") : null;
            final int customers = ticket.getInt("customers");

            try {
                if (MainActivity.isPrinterConnected) {
                    MainActivity.binder.writeDataByYouself(
                            new UiExecute() {
                                @Override
                                public void onsucess() {

                                }

                                @Override
                                public void onfailed() {
                                    Log.e("WRITE", "ERROR");
                                }
                            }, new ProcessData() {
                                @Override
                                public List<byte[]> processDataBeforeSend() {
                                    List<byte[]> list = new ArrayList<byte[]>();

                                    //creat a text ,and make it to byte[],
                                    String str = company_name + "\n" + "Your Ticket Number" + "\n" + ticket_no + "\n" + "\n" + "Please be seated we will be attending to you shortly." + "\n" + "Latest Ticket Served: " + serving + "\n" + "Total Customer(s) waiting: " + customers + "\n" + "\n" + "\n" + date + "\t\t\t\t" + time;
                                    list.add(DataForSendToPrinterPos58.initializePrinter());

                                    list.add(DataForSendToPrinterPos58.selectOrCancelBoldModel(1));
                                    list.add(DataForSendToPrinterPos58.selectAlignment(1));
                                    list.add(StringUtils.strTobytes(company_name.replace(" Philippines", "")));
                                    list.add(DataForSendToPrinterPos58.printAndFeedLine());

                                    list.add(DataForSendToPrinterPos58.selectAlignment(1));
                                    list.add(StringUtils.strTobytes(company_name.replace("Development Bank of the ", "")));
                                    list.add(DataForSendToPrinterPos58.printAndFeedLine());

                                    list.add(DataForSendToPrinterPos58.selectOrCancelBoldModel(0));
                                    list.add(DataForSendToPrinterPos58.selectAlignment(1));
                                    list.add(StringUtils.strTobytes("\nYour Ticket Number"));
                                    list.add(DataForSendToPrinterPos58.printAndFeedLine());

                                    list.add(DataForSendToPrinterPos58.selectOrCancelBoldModel(1));
                                    list.add(DataForSendToPrinterPos58.selectCharacterSize(7));
                                    list.add(DataForSendToPrinterPos58.selectCharacterSize(7));
                                    list.add(DataForSendToPrinterPos58.selectAlignment(1));
                                    list.add(StringUtils.strTobytes("\n" + ticket_no));
                                    list.add(DataForSendToPrinterPos58.printAndFeedLine());

                                    list.add(DataForSendToPrinterPos58.selectCharacterSize(4));
                                    list.add(DataForSendToPrinterPos58.selectCharacterSize(0));
                                    list.add(DataForSendToPrinterPos58.printAndFeedLine());

                                    list.add(DataForSendToPrinterPos58.selectOrCancelBoldModel(0));
                                    list.add(DataForSendToPrinterPos58.selectOrCancelDoubelPrintModel(0));
                                    list.add(DataForSendToPrinterPos58.selectAlignment(1));
                                    list.add(StringUtils.strTobytes("Please be seated we will be\nattending to you shortly." + "\n"));
                                    list.add(DataForSendToPrinterPos58.printAndFeedLine());

                                    if (serving != null) {
                                        list.add(DataForSendToPrinterPos58.selectAlignment(1));
                                        list.add(StringUtils.strTobytes("Latest Ticket Served: " + serving + "\n"));
                                        list.add(DataForSendToPrinterPos58.printAndFeedLine());
                                    }

                                    if (customers > 0) {
                                        list.add(DataForSendToPrinterPos58.selectAlignment(1));
                                        list.add(StringUtils.strTobytes("Total Customer(s) waiting: " + customers + "\n"));
                                        list.add(DataForSendToPrinterPos58.printAndFeedLine());
                                    }

                                    list.add(DataForSendToPrinterPos58.selectAlignment(1));
                                    list.add(StringUtils.strTobytes("\n" + date + " --- " + time));
                                    list.add(DataForSendToPrinterPos58.printAndFeedLine());
                                    list.add(DataForSendToPrinterPos58.printAndFeedLine());
                                    list.add(DataForSendToPrinterPos58.printAndFeedLine());
                                    list.add(DataForSendToPrinterPos58.printAndFeedLine());
                                    list.add(DataForSendToPrinterPos58.printAndFeedLine());
                                    list.add(DataForSendToPrinterPos80.selectCutPagerModerAndCutPager(1));

                                    return list;
                                }
                            });
                } else {
                }
            } catch (Exception e) {
                Sentry.capture(e);
            }
        } catch (JSONException e) {
            Sentry.capture(e);
        }
    }
}