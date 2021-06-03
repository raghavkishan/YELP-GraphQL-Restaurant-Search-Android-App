package com.example.yelpapirestaurantsearch.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.yelpapirestaurantsearch.R
import com.example.yelpapirestaurantsearch.SearchRestaurantsQuery
import org.w3c.dom.Text


/**
 *
 * To display the list of restaurants
 */
class RestaurantRecyclerViewAdapter(
    var restaurantBusinesses: List<SearchRestaurantsQuery.Business?>):
    RecyclerView.Adapter<RestaurantRecyclerViewAdapter.RestaurantViewHolder>() {

    var itemClickListener: ItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RestaurantViewHolder {
        return RestaurantViewHolder(
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.restaurant_list_item_view, parent, false))
    }

    override fun onBindViewHolder(holder: RestaurantViewHolder, position: Int) {
        holder.restaurantTitleView.text = restaurantBusinesses[position]?.name
        holder.restaurantAddressView.text = restaurantBusinesses[position]?.location?.address1
        holder.restaurantCostIndicatorView.text = restaurantBusinesses[position]?.price
        holder.restaurantPhoneNumberView.text = restaurantBusinesses[position]?.display_phone

        holder.restaurantAddressView.apply {
            transitionName = restaurantBusinesses[position]?.location?.address1
        }
    }

    override fun getItemCount(): Int {
        return restaurantBusinesses.size
    }

    fun setData(businesses: List<SearchRestaurantsQuery.Business?>){
        this.restaurantBusinesses = businesses
        notifyDataSetChanged()
    }


    inner class RestaurantViewHolder(itemView: View):
        RecyclerView.ViewHolder(itemView),
        View.OnClickListener{

       val restaurantTitleView: TextView = itemView.findViewById(R.id.restaurant_title_text_view)
       val restaurantAddressView: TextView = itemView.findViewById(R.id.restaurant_address_text_view)
       val restaurantCostIndicatorView: TextView = itemView.findViewById(R.id.restaurant_cost_indicator_view)
       val restaurantPhoneNumberView: TextView = itemView.findViewById(R.id.restaurant_phone_view)

       init {
           itemView.setOnClickListener(this)
       }

        override fun onClick(p0: View?) {
            onItemClicked?.invoke(getItem(adapterPosition), restaurantAddressView)
        }

    }

    var onItemClicked: ((SearchRestaurantsQuery.Business, TextView) -> Unit)? = null

    interface ItemClickListener{
        fun onItemClick(view: View?, position: Int)
    }

    fun getItem(id: Int): SearchRestaurantsQuery.Business{
        return restaurantBusinesses[id]!!;
    }
}

