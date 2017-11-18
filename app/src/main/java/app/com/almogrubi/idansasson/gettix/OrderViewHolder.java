package app.com.almogrubi.idansasson.gettix;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import app.com.almogrubi.idansasson.gettix.entities.Order;
import app.com.almogrubi.idansasson.gettix.utilities.DataUtils;
import app.com.almogrubi.idansasson.gettix.utilities.Utils;

/**
 * Created by idans on 17/11/2017.
 *
 * A ViewHolder is a required part of the pattern for RecyclerViews. It mostly behaves as
 * a cache of the child views for a forecast item. It's also a convenient place to set an
 * OnClickListener, since it has access to the adapter and the views.
 *
 */

public class OrderViewHolder extends RecyclerView.ViewHolder {

    private TextView tvCustomerName;
    private TextView tvCustomerPhone;
    private TextView tvCustomerEmail;
    private TextView tvOrderDate;
    private TextView tvTotalPrice;
    private TextView tvConfirmationNumber;
    private ImageView ivCouponUsed;

    public OrderViewHolder(View itemView) {
        super(itemView);
        this.tvCustomerName = itemView.findViewById(R.id.tv_order_item_customer_name);
        this.tvCustomerPhone = itemView.findViewById(R.id.tv_order_item_customer_phone);
        this.tvCustomerEmail = itemView.findViewById(R.id.tv_order_item_customer_email);
        this.tvOrderDate = itemView.findViewById(R.id.tv_order_item_date);
        this.tvTotalPrice = itemView.findViewById(R.id.tv_order_item_total_price);
        this.tvConfirmationNumber = itemView.findViewById(R.id.tv_order_item_confirmation_number);
        this.ivCouponUsed = itemView.findViewById(R.id.iv_order_item_coupon_used);
    }

    public void bindOrder(Order order) {
        tvCustomerName.setText(String.format("%s - %d כרטיסים", order.getCustomer().getName(), order.getTicketsNum()));
        tvCustomerPhone.setText(order.getCustomer().getPhone());
        tvCustomerEmail.setText(order.getCustomer().getEmail());
        tvOrderDate.setText(DataUtils.convertToUiDateFormat(order.getCreationDateLong()));
        tvTotalPrice.setText(String.format("%d₪", order.getTotalPrice()));
        tvConfirmationNumber.setText("מזהה: " + order.getConfirmationNumber());

        if (order.isCouponUsed())
            ivCouponUsed.setVisibility(View.VISIBLE);
        else
            ivCouponUsed.setVisibility(View.INVISIBLE);
    }
}
