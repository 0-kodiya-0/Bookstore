// Enhanced JavaScript utilities for Bookstore frontend with Tailwind CSS

document.addEventListener('DOMContentLoaded', function() {
    // Initialize quantity input fields in the cart
    initializeQuantityInputs();
    
    // Auto-hide alert messages after a delay
    initializeAlertDismissal();
    
    // Add smooth scrolling for anchor links
    initializeSmoothScrolling();
    
    // Initialize dropdown menus
    initializeDropdowns();
});

/**
 * Initialize quantity input fields with better UI for increment/decrement
 */
function initializeQuantityInputs() {
    const quantityInputs = document.querySelectorAll('.quantity-input');
    
    quantityInputs.forEach(input => {
        // Create a wrapper div for the input field
        const wrapper = document.createElement('div');
        wrapper.className = 'flex items-center border border-gray-300 rounded overflow-hidden';
        
        // Create decrement button
        const decrementBtn = document.createElement('button');
        decrementBtn.type = 'button';
        decrementBtn.className = 'px-2 py-1 bg-gray-100 hover:bg-gray-200 text-gray-600 transition-colors focus:outline-none';
        decrementBtn.innerHTML = '−';
        decrementBtn.addEventListener('click', function() {
            const currentValue = parseInt(input.value) || 1;
            input.value = Math.max(1, currentValue - 1);
            triggerChangeEvent(input);
        });
        
        // Create increment button
        const incrementBtn = document.createElement('button');
        incrementBtn.type = 'button';
        incrementBtn.className = 'px-2 py-1 bg-gray-100 hover:bg-gray-200 text-gray-600 transition-colors focus:outline-none';
        incrementBtn.innerHTML = '+';
        incrementBtn.addEventListener('click', function() {
            const currentValue = parseInt(input.value) || 1;
            input.value = currentValue + 1;
            triggerChangeEvent(input);
        });
        
        // Update input styling
        input.className = 'text-center border-0 py-1 w-12 focus:ring-0 focus:outline-none';
        
        // Add keyboard functionality
        input.addEventListener('keydown', function(e) {
            if (e.key === 'ArrowUp') {
                e.preventDefault();
                incrementBtn.click();
            } else if (e.key === 'ArrowDown') {
                e.preventDefault();
                decrementBtn.click();
            }
        });
        
        // Validate the input to ensure it's a positive number
        input.addEventListener('change', function() {
            const value = parseInt(this.value) || 1;
            this.value = Math.max(1, value);
        });
        
        // Replace input with the new wrapper
        const parentElement = input.parentElement;
        wrapper.appendChild(decrementBtn);
        wrapper.appendChild(input);
        wrapper.appendChild(incrementBtn);
        parentElement.appendChild(wrapper);
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
 * Initialize auto-dismissal of alert messages
 */
function initializeAlertDismissal() {
    const alerts = document.querySelectorAll('.p-4.my-4.bg-blue-100, .p-4.my-4.bg-red-100');
    
    alerts.forEach(alert => {
        // Add close button
        const closeButton = document.createElement('button');
        closeButton.innerHTML = '&times;';
        closeButton.className = 'ml-4 text-current font-bold text-xl leading-none focus:outline-none';
        closeButton.setAttribute('aria-label', 'Close');
        closeButton.addEventListener('click', () => dismissAlert(alert));
        
        // Make the alert relative for positioning the close button
        alert.classList.add('relative');
        
        // Add close button to alert
        alert.appendChild(closeButton);
        
        // Auto-dismiss after 5 seconds
        setTimeout(() => dismissAlert(alert), 5000);
    });
}

/**
 * Dismiss an alert with fade-out animation
 * @param {HTMLElement} alert - The alert element to dismiss
 */
function dismissAlert(alert) {
    alert.classList.add('opacity-0', 'transition-opacity', 'duration-300');
    
    // Remove from DOM after transition
    setTimeout(() => {
        if (alert.parentNode) {
            alert.parentNode.removeChild(alert);
        }
    }, 300);
}

/**
 * Initialize smooth scrolling for anchor links
 */
function initializeSmoothScrolling() {
    document.querySelectorAll('a[href^="#"]').forEach(anchor => {
        anchor.addEventListener('click', function(e) {
            e.preventDefault();
            
            const targetId = this.getAttribute('href');
            if (targetId === '#') return;
            
            const targetElement = document.querySelector(targetId);
            if (targetElement) {
                targetElement.scrollIntoView({
                    behavior: 'smooth',
                    block: 'start'
                });
            }
        });
    });
}

/**
 * Initialize dropdown menus
 */
function initializeDropdowns() {
    const dropdowns = document.querySelectorAll('.dropdown');
    
    dropdowns.forEach(dropdown => {
        const trigger = dropdown.querySelector('.dropdown-trigger');
        const menu = dropdown.querySelector('.dropdown-menu');
        
        if (trigger && menu) {
            trigger.addEventListener('click', function(e) {
                e.preventDefault();
                menu.classList.toggle('hidden');
            });
            
            // Close dropdown when clicking outside
            document.addEventListener('click', function(e) {
                if (!dropdown.contains(e.target)) {
                    menu.classList.add('hidden');
                }
            });
        }
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

/**
 * Truncate text to a specified length and add ellipsis
 * @param {string} text - The text to truncate
 * @param {number} length - The maximum length (default: 100)
 * @returns {string} The truncated text
 */
function truncateText(text, length = 100) {
    if (!text || text.length <= length) return text;
    return text.substring(0, length) + '...';
}

/**
 * Debounce function to limit how often a function is called
 * @param {Function} func - The function to debounce
 * @param {number} wait - The time to wait in milliseconds
 * @returns {Function} The debounced function
 */
function debounce(func, wait = 300) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}