package com.example.fragment;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.adapter.FilterAdapter;
import com.example.jobs.R;
import com.example.jobs.databinding.LayoutFilterSheetBinding;
import com.example.model.Filter;
import com.example.util.AppUtil;
import com.example.util.GeneralUtils;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;

public class FilterBottomFragment extends BottomSheetDialogFragment {

    LayoutFilterSheetBinding viewBinding;
    TextView[] allTv;
    ArrayList<Filter> filterList;
    FilterAdapter filterAdapter;
    FilterButtonOnClickListener filterButtonOnClickListener;
    float minSalary, maxSalary, minSalarySelected, maxSalarySelected;
    String filterSel = Filter.CATEGORY;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        viewBinding = LayoutFilterSheetBinding.inflate(inflater, container, false);
        filterList = new ArrayList<>();
        boolean isRTL = Boolean.parseBoolean(getString(R.string.isRTL));
        if (isRTL) {
            requireActivity().getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }
        if (getArguments() != null) {
            filterList = getArguments().getParcelableArrayList("filterList");
            minSalary = getArguments().getFloat("minSalary");
            maxSalary = getArguments().getFloat("maxSalary");
            minSalarySelected = getArguments().getFloat("minSalarySelected");
            maxSalarySelected = getArguments().getFloat("maxSalarySelected");
        }

        allTv = new TextView[]{viewBinding.tvCategory, viewBinding.tvCompany, viewBinding.tvCity, viewBinding.tvJobType, viewBinding.tvQualification, viewBinding.tvSalary};

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        viewBinding.rvFilter.setLayoutManager(layoutManager);
        filterAdapter = new FilterAdapter(getActivity());
        viewBinding.rvFilter.setAdapter(filterAdapter);
        viewBinding.rvFilter.addItemDecoration(GeneralUtils.listItemDecoration(getActivity(), R.dimen.item_space_filter));

        changeColorAndBackground(viewBinding.tvCategory);
        filterAdapter.setList(getFilterTypeList(Filter.CATEGORY));

        viewBinding.tvCategory.setOnClickListener(view -> {
            filterSel = Filter.CATEGORY;
            showSalaryView(false);
            changeColorAndBackground(viewBinding.tvCategory);
            filterAdapter.setList(getFilterTypeList(Filter.CATEGORY));
        });

        viewBinding.tvCompany.setOnClickListener(view -> {
            filterSel = Filter.COMPANY;
            showSalaryView(false);
            changeColorAndBackground(viewBinding.tvCompany);
            filterAdapter.setList(getFilterTypeList(Filter.COMPANY));
        });

        viewBinding.tvCity.setOnClickListener(view -> {
            filterSel = Filter.CITY;
            showSalaryView(false);
            changeColorAndBackground(viewBinding.tvCity);
            filterAdapter.setList(getFilterTypeList(Filter.CITY));
        });

        viewBinding.tvJobType.setOnClickListener(view -> {
            filterSel = Filter.JOB_TYPE;
            showSalaryView(false);
            changeColorAndBackground(viewBinding.tvJobType);
            filterAdapter.setList(getFilterTypeList(Filter.JOB_TYPE));
        });

        viewBinding.tvQualification.setOnClickListener(view -> {
            filterSel = Filter.QUALIFICATION;
            showSalaryView(false);
            changeColorAndBackground(viewBinding.tvQualification);
            filterAdapter.setList(getFilterTypeList(Filter.QUALIFICATION));
        });

        viewBinding.tvSalary.setOnClickListener(view -> {
            showSalaryView(true);
            changeColorAndBackground(viewBinding.tvSalary);
        });

        viewBinding.ivClose.setOnClickListener(view -> dismiss());
        viewBinding.btnClearFilter.setOnClickListener(view -> resetFilter());
        viewBinding.btnApplyFilter.setOnClickListener(view -> {
            ArrayList<Filter> filterSelectedList = new ArrayList<>();
            for (Filter itemFilter : filterList) {
                if (itemFilter.isSelected()) {
                    filterSelectedList.add(itemFilter);
                }
            }
            if (minSalarySelected != 0 || maxSalarySelected != 0) {
                String salary = getString(R.string.filter_salary_sel, AppUtil.currencyCode, GeneralUtils.viewFormat(String.valueOf(minSalarySelected)), GeneralUtils.viewFormat(String.valueOf(maxSalarySelected)));
                filterSelectedList.add(new Filter("-1", salary, Filter.SALARY, false));
            }
            filterButtonOnClickListener.onButtonClick(filterSelectedList, minSalarySelected, maxSalarySelected);
            dismiss();
        });

        setSalaryRange();
        viewBinding.rangeSalary.addOnChangeListener((slider, value, fromUser) -> {
            if (fromUser) {
                minSalarySelected = slider.getValues().get(0);
                maxSalarySelected = slider.getValues().get(1);
                viewBinding.tvSalaryStart.setText(getString(R.string.salary_currency, AppUtil.currencyCode, GeneralUtils.viewFormat(String.valueOf(minSalarySelected))));
                viewBinding.tvSalaryEnd.setText(getString(R.string.salary_currency, AppUtil.currencyCode, GeneralUtils.viewFormat(String.valueOf(maxSalarySelected))));
            }
        });
        return viewBinding.getRoot();
    }

    private void setSalaryRange() {
        try {
            viewBinding.rangeSalary.setValueTo(maxSalary);
            viewBinding.rangeSalary.setValueFrom(minSalary);
            viewBinding.rangeSalary.setValues(minSalarySelected == 0 ? minSalary : minSalarySelected, maxSalarySelected == 0 ? maxSalary : maxSalarySelected);
            viewBinding.rangeSalary.setStepSize(minSalary < 100 ? minSalary : 100);
            viewBinding.tvSalaryStart.setText(getString(R.string.salary_currency, AppUtil.currencyCode, GeneralUtils.viewFormat(String.valueOf(minSalarySelected == 0 ? minSalary : minSalarySelected))));
            viewBinding.tvSalaryEnd.setText(getString(R.string.salary_currency, AppUtil.currencyCode, GeneralUtils.viewFormat(String.valueOf(maxSalarySelected == 0 ? maxSalary : maxSalarySelected))));

        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    private void showSalaryView(boolean flag) {
        if (flag) {
            viewBinding.rlSalary.setVisibility(View.VISIBLE);
            viewBinding.rvFilter.setVisibility(View.GONE);
        } else {
            viewBinding.rlSalary.setVisibility(View.GONE);
            viewBinding.rvFilter.setVisibility(View.VISIBLE);
        }
    }

    private ArrayList<Filter> getFilterTypeList(String type) {
        ArrayList<Filter> list = new ArrayList<>();
        for (Filter itemFilter : filterList) {
            if (itemFilter.getFilterType().equals(type)) {
                list.add(itemFilter);
            }
        }
        return list;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void resetFilter() {
        for (int i = 0; i < filterList.size(); i++) {
            Filter itemFilter = filterList.get(i);
            if (itemFilter.isSelected()) {
                filterList.set(i, new Filter(itemFilter.getFilterId(), itemFilter.getFilterName(), itemFilter.getFilterType(), false));
            }
        }
        filterAdapter.setList(getFilterTypeList(filterSel));
        minSalarySelected = 0;
        maxSalarySelected = 0;
        setSalaryRange();
    }

    private void changeColorAndBackground(TextView textView) {
        for (TextView tv : allTv) {
            if (tv == textView) {
                tv.setTextColor(getResources().getColor(R.color.white));
                tv.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            } else {
                tv.setTextColor(getResources().getColor(R.color.subTitle_80));
                tv.setBackgroundColor(Color.TRANSPARENT);
            }
        }
    }

    public void setOnFilterButtonClickListener(FilterButtonOnClickListener clickListener) {
        this.filterButtonOnClickListener = clickListener;
    }

    public interface FilterButtonOnClickListener {
        void onButtonClick(ArrayList<Filter> mFilterList, float minSalary, float maxSalary);
    }

    @Override
    public int getTheme() {
        return R.style.BottomSheetDialog;
    }
}
