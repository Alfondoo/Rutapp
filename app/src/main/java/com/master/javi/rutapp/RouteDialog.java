package com.master.javi.rutapp;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.master.javi.rutapp.data.Route;


public class RouteDialog extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.dialog_save, null);
        final EditText name = (EditText) v.findViewById(R.id.dialog_name);
        final EditText description = (EditText) v.findViewById(R.id.dialog_description);
        builder.setView(v)
                .setPositiveButton("Guardar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ((MapsActivity) getActivity()).createRoute(name.getText().toString(), description.getText().toString());
                    }
                })
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
        final AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
                name.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
                        if(TextUtils.isEmpty(editable) || TextUtils.isEmpty(description.getText())){
                            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
                        }
                        else{
                            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
                        }
                    }
                });
                description.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
                        if(TextUtils.isEmpty(editable) || TextUtils.isEmpty(name.getText())){
                            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
                        }
                        else{
                            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
                        }
                    }
                });
            }
        });
        return dialog;
    }
}
