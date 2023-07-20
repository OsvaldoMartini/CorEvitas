import React, { useState } from "react";
import "../Styles/Troller.css";

const Troller = () => {
  const [items, setItems] = useState([
    { name: "BOOK", price: 5, quantity: 0 },
    { name: "CD", price: 10, quantity: 0 },
    { name: "DVD", price: 15, quantity: 0 },
  ]);

  const [history, setHistory] = useState([]);
  const today = new Date();
  const isThursday = today.getDay() === 4; // Thursday has index 4 (Sunday is 0, Monday is 1, and so on)

  const calculateTotal = () => {
    let total = 0;
    let totalItems = 0;
    let totalDiscount = 0;

    items.forEach((item) => {
      const { name, price, quantity } = item;
      const totalPrice = price * quantity;

      totalItems += quantity;
      total += totalPrice;

      if (isThursday) {
        if (name === "DVD") {
          const dvdsDiscount = Math.floor(quantity / 2);
          const dvdPrice = dvdsDiscount * price;
          totalDiscount += dvdPrice;
        } else if (name === "BOOK" || name === "CD") {
          const itemDiscount = Math.floor(quantity / 2) * price;
          totalDiscount += itemDiscount;
        }
      }
    });

    if (isThursday) {
      const thursdayDiscount = 0.2 * total;
      totalDiscount += thursdayDiscount;
    }

    const finalTotal = total - totalDiscount;

    return {
      totalItems,
      total,
      totalDiscount,
      finalTotal,
    };
  };

  const handleQuantityChange = (index, value) => {
    const newItems = [...items];
    newItems[index].quantity = value;
    setItems(newItems);
  };

  const handleCheckout = () => {
    const { totalItems, totalDiscount } = calculateTotal();
    const historyEntry = `Items: ${totalItems} Discount: £${totalDiscount.toFixed(
      2
    )}`;
    setHistory((prevHistory) => [...prevHistory, historyEntry]);
  };

  const { totalItems, total, totalDiscount, finalTotal } = calculateTotal();

  return (
    <div className="troller-container">
      <div className="left-column">
        <div className="history-box">
          <h2>History</h2>
          {history.map((entry, index) => (
            <p key={index} className="history-entry">
              {entry}
            </p>
          ))}
        </div>
        <div className="summary-box">
          <p>Summary of Selected Troillies</p>
          <div className="total-item">
            <p>Total: £{finalTotal.toFixed(2)}</p>
          </div>
          <div className="total-item">
            <p>Total Items: {totalItems}</p>
          </div>
          <div className="total-item">
            <p>Total Discount: £{totalDiscount.toFixed(2)}</p>
          </div>
        </div>
      </div>
      <div className="right-column">
        <div className="products-box">
          <div className="product-header">
            <div className="product-item-header">Product</div>
            <div className="product-item-header">Price</div>
            <div className="product-item-header">Quantity</div>
            <div className="product-item-header">Total</div>
          </div>
          {items.map((item, index) => (
            <div className="product-row" key={item.name}>
              <div className="product-item">{item.name}</div>
              <div className="product-item">£{item.price}</div>
              <div className="product-item">
                <button
                  onClick={() => handleQuantityChange(index, item.quantity - 1)}
                >
                  -
                </button>
                {item.quantity}
                <button
                  onClick={() => handleQuantityChange(index, item.quantity + 1)}
                >
                  +
                </button>
              </div>
              <div className="product-item">
                £{(item.price * item.quantity).toFixed(2)}
              </div>
            </div>
          ))}
        </div>
        <div className="total-box">
          <div className="total-item">
            <p>Total: £{total.toFixed(2)}</p>
          </div>
          <button onClick={handleCheckout}>Checkout</button>
        </div>
      </div>
    </div>
  );
};

export default Troller;
