const stripe = Stripe('pk_test_51PfM1lK56XZ4pCuCNrhhlmbG0uqByy8fghmPpxVtcRUGDRg75tXsRg4xKmWxmIzPTWtELzafgXcsVTpp6NhlQGEn00SRbWupfD');
const paymentButton = document.querySelector('#paymentButton');

paymentButton.addEventListener('click', () => {
	stripe.redirectToCheckout({
		sessionId: sessionId
	})
});
