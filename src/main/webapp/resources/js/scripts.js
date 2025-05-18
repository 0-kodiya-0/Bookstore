// JavaScript utilities for Bookstore frontend

document.addEventListener('DOMContentLoaded', function() {
    // Initialize quantity input fields in the cart
    initializeQuantityInputs();
    
    // Initialize any tooltip functionality
    initializeTooltips();
    
    // Auto-hide alert messages after a delay
    initializeAlertDismissal();
});

/**
 * Initialize quantity input fields in the shopping cart
 */
function initializeQuantityInputs() {
    const quantityInputs = document.querySelectorAll('.quantity-input');
    
    quantityInputs.forEach(input => {
        // Add event listeners for keyboard up/down
        input.addEventListener('keydown', function(e) {
            if (e.key === 'ArrowUp') {
                e.preventDefault();
                this.value = parseInt(this.value) + 1 || 1;
                triggerChangeEvent(this);
            } else if (e.key === 'ArrowDown') {
                e.preventDefault();
                this.value = Math.max(1, parseInt(this.value) - 1 || 1);
                triggerChangeEvent(this);
            }
        });
        
        // Validate the input to ensure it's a positive number
        input.addEventListener('change', function() {
            const value = parseInt(this.value) || 1;
            this.value = Math.max(1, value);
        });
    });
}

/**
 * Trigger a change event on an element
 * @param {HTMLElement} element - The element to trigger the change event on
 */
function triggerChangeEvent(element) {
    const event = new Event('change', { bubbles: true });
    element.dispatchEvent(event);
}

/**
 * Initialize any tooltip functionality
 */
function initializeTooltips() {
    const tooltipElements = document.querySelectorAll('[data-tooltip]');
    
    tooltipElements.forEach(element => {
        element.addEventListener('mouseenter', function() {
            const tooltip = this.getAttribute('data-tooltip');
            
            if (!tooltip) return;
            
            // Create tooltip element
            const tooltipEl = document.createElement('div');
            tooltipEl.className = 'tooltip';
            tooltipEl.textContent = tooltip;
            
            // Position tooltip
            const rect = this.getBoundingClientRect();
            tooltipEl.style.top = rect.top + window.scrollY - 30 + 'px';
            tooltipEl.style.left = rect.left + window.scrollX + (rect.width / 2) + 'px';
            tooltipEl.style.transform = 'translateX(-50%)';
            
            // Add to DOM
            document.body.appendChild(tooltipEl);
            
            // Store reference to tooltip
            this.tooltipElement = tooltipEl;
        });
        
        element.addEventListener('mouseleave', function() {
            if (this.tooltipElement) {
                document.body.removeChild(this.tooltipElement);
                this.tooltipElement = null;
            }
        });
    });
}

/**
 * Initialize auto-dismissal of alert messages
 */
function initializeAlertDismissal() {
    const alerts = document.querySelectorAll('.alert');
    
    alerts.forEach(alert => {
        // Auto-dismiss after 5 seconds
        setTimeout(() => {
            if (alert.parentNode) {
                alert.style.opacity = '0';
                
                // Remove from DOM after transition
                setTimeout(() => {
                    if (alert.parentNode) {
                        alert.parentNode.removeChild(alert);
                    }
                }, 300);
            }
        }, 5000);
        
        // Add click-to-dismiss functionality
        alert.addEventListener('click', function() {
            this.style.opacity = '0';
            
            // Remove from DOM after transition
            setTimeout(() => {
                if (this.parentNode) {
                    this.parentNode.removeChild(this);
                }
            }, 300);
        });
    });
}

/**
 * Format currency values
 * @param {number} value - The value to format
 * @param {string} currency - The currency code (default: USD)
 * @returns {string} The formatted currency string
 */
function formatCurrency(value, currency = 'USD') {
    return new Intl.NumberFormat('en-US', { 
        style: 'currency', 
        currency: currency 
    }).format(value);
}

/**
 * Format date values
 * @param {string|Date} date - The date to format
 * @param {string} format - The format to use (default: 'medium')
 * @returns {string} The formatted date string
 */
function formatDate(date, format = 'medium') {
    const dateObj = typeof date === 'string' ? new Date(date) : date;
    
    const options = {
        short: { month: 'numeric', day: 'numeric', year: '2-digit' },
        medium: { month: 'short', day: 'numeric', year: 'numeric' },
        long: { month: 'long', day: 'numeric', year: 'numeric' }
    };
    
    return new Intl.DateTimeFormat('en-US', options[format] || options.medium).format(dateObj);
}
